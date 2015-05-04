package io.github.martinjos.jldd;

import java.util.zip.*;
import java.util.*;
import java.io.*;

public class Main {

    private static enum ConstantType {
        Class(7, 2),
        Fieldref(9, 4),
        Methodref(10, 4),
        InterfaceMethodref(11, 4),
        String(8, 2),
        Integer(3, 4),
        Float(4, 4),
        Long(5, 8),
        Double(6, 8),
        NameAndType(12, 4),
        Utf8(1, 2), // actual length is 2 + value of length field
        MethodHandle(15, 3),
        MethodType(16, 2),
        InvokeDynamic(18, 4);

        private static ConstantType[] lookup = new ConstantType[19];
        public static ConstantType byTag(int tag) {
            if (lookup[1] == null) {
                for (ConstantType ct : ConstantType.values()) {
                    lookup[ct.getTag()] = ct;
                }
            }
            return lookup[tag];
        }

        private int tag, length;
        public int getTag() { return tag; }
        public int getLength() { return length; }

        ConstantType(int tag, int length) {
            this.tag = tag;
            this.length = length;
        }
    }

    private static void saveClassLookups(Map<String,String> map, String filename)
    throws IOException {
        ZipFile f = new ZipFile(filename);
        for (ZipEntry ent : new EnumerationIterable<ZipEntry>(f.entries())) {
            if (!ent.isDirectory()) {
                String name = ent.getName();
                if (name.toLowerCase().endsWith(".class")) {
                    name = name.replaceFirst("(?i)\\.class$", "").replaceAll("[/\\\\$]", ".");
                    if (name.matches(".*\\.[0-9]+(?:\\.|$)")) {
                        continue; // anonymous
                    }
                    if (!map.containsKey(name)) {
                        map.put(name, filename);
                    }
                }
            }
        }
    }

    private static Set<String> getRefsUsed(String filename, Map<String,String> lookups)
    throws IOException {
        ZipFile f = new ZipFile(filename);
        Set<String> types = new TreeSet<String>();
        for (ZipEntry ent : new EnumerationIterable<ZipEntry>(f.entries())) {
            if (!ent.isDirectory() && ent.getName().toLowerCase().endsWith(".class")) {
                System.out.println("Processing " + ent);
                DataInputStream dis = new DataInputStream(f.getInputStream(ent));
                dis.skipBytes(8);
                int cpCount = dis.readUnsignedShort();
                Map<Integer,String> utf8 = new HashMap<Integer,String>();
                Set<Integer> validIdxs = new HashSet<Integer>();
                for (int i = 1; i < cpCount; i++) {
                    int typeTag = dis.readUnsignedByte();
                    ConstantType type = ConstantType.byTag(typeTag);
                    if (type == ConstantType.Utf8) {
                        //System.out.println("Got " + type);
                        int utf8len = dis.readUnsignedShort();
                        byte[] bytes = new byte[utf8len];
                        dis.readFully(bytes);
                        String str = null;
                        try {
                            str = new String(bytes, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        utf8.put(i, str);
                    } else if (type == null) {
                        System.out.println("Error: got null tag in "+filename+"/"+ent.getName()+" (tag = "+typeTag+"): ending at index "+i+"; cpCount = " + cpCount);
                        break;
                    } else if (type == ConstantType.Class || type == ConstantType.MethodType) {
                        validIdxs.add(dis.readUnsignedShort());
                    } else if (type == ConstantType.NameAndType) {
                        dis.skipBytes(2);
                        validIdxs.add(dis.readUnsignedShort());
                    } else {
                        //if (type != ConstantType.String && type != ConstantType.Methodref && type != ConstantType.Fieldref && type != ConstantType.InterfaceMethodref) {
                            //System.out.println("Got " + type);
                        //}
                        if (type == ConstantType.Long || type == ConstantType.Double) {
                            i++;
                        }
                        dis.skipBytes(type.getLength());
                    }
                }
                System.out.println("utf8.size() == " + utf8.size());
                System.out.println("validIdxs.size() == " + validIdxs.size());
                for (int idx : validIdxs) {
                    if (!utf8.containsKey(idx)) {
                        System.out.println("ERROR: Index is not valid: " + idx);
                        continue;
                    }
                    types.add(utf8.get(idx));
                }
            }
        }
        for (String type : types) {
            System.out.println(type);
        }
        return types;
    }

    public static void main(String[] args) throws IOException {
        Map<String,String> lookups = new TreeMap<String,String>();
        saveClassLookups(lookups, args[0]);
        String[] cp = args[1].split(";");
        for (String cpstr : cp) {
            saveClassLookups(lookups, cpstr);
        }
        for (String key : getRefsUsed(args[0], lookups)) {
            //System.out.println(key + ": " + lookups.get(key));
        }
    }
}

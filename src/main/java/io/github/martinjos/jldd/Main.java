package io.github.martinjos.jldd;

import java.util.zip.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class Main {

    private static String NOT_FOUND = "<some symbols not resolved>";

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
                    name = name.replaceFirst("(?i)\\.class$", "");
                    // Leave it in the form used by the symbol table.
                    //.replaceAll("[/\\\\$]", ".");
                    if (name.matches(".*\\.[0-9]+(?:\\.|$).*")) {
                        continue; // anonymous
                    }
                    if (!map.containsKey(name)) {
                        map.put(name, filename);
                    }
                }
            }
        }
    }

    private static Set<String> getTypesUsed(String filename)
    throws IOException {
        ZipFile f = new ZipFile(filename);
        Set<String> types = new TreeSet<String>();
        for (ZipEntry ent : new EnumerationIterable<ZipEntry>(f.entries())) {
            if (!ent.isDirectory() && ent.getName().toLowerCase().endsWith(".class")) {
                //System.out.println("Processing " + ent);
                DataInputStream dis = new DataInputStream(f.getInputStream(ent));
                dis.skipBytes(8);
                int cpCount = dis.readUnsignedShort();
                Map<Integer,String> utf8 = new HashMap<Integer,String>();
                Set<Integer> validIdxs = new HashSet<Integer>();
                Set<Integer> validIdxsBare = new HashSet<Integer>();
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
                        //System.out.println("Error: got null tag in "+filename+"/"+ent.getName()+" (tag = "+typeTag+"): ending at index "+i+"; cpCount = " + cpCount);
                        break;
                    } else if (type == ConstantType.Class) {
                        // Bare class names, apart from array classes
                        validIdxsBare.add(dis.readUnsignedShort());
                    } else if (type == ConstantType.MethodType) {
                        // I assume this is in standard form
                        validIdxs.add(dis.readUnsignedShort());
                    } else if (type == ConstantType.NameAndType) {
                        // Standard form
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
                //System.out.println("utf8.size() == " + utf8.size());
                //System.out.println("validIdxs.size() == " + validIdxs.size());
                //System.out.println("validIdxsBare.size() == " + validIdxsBare.size());
                for (int idx : validIdxs) {
                    if (!utf8.containsKey(idx)) {
                        System.err.println("ERROR: Index is not valid: " + idx);
                        continue;
                    }
                    types.add(utf8.get(idx));
                }
                for (int idx : validIdxsBare) {
                    if (!utf8.containsKey(idx)) {
                        System.err.println("ERROR: Index is not valid: " + idx);
                        continue;
                    }
                    String typeStr = utf8.get(idx);
                    if (!typeStr.startsWith("[")) {
                        // Ensure that all class names can be extracted in the
                        // same way
                        typeStr = "#L" + typeStr + ";";
                    }
                    types.add(typeStr);
                }
            }
        }
        return types;
    }

    private static void extractRefsFromType(Set<String> refs, String type, Map<String,String> lookups) {
        Pattern p = Pattern.compile("L([^;]*);");
        Matcher m = p.matcher(type);
        while (m.find()) {
            String name = m.group(1);
            //refs.add(name); // names instead of refs
            if (lookups.containsKey(name)) {
                refs.add(lookups.get(name));
            } else {
                //System.err.println("WARNING: Symbol not found: " + name);
                refs.add(NOT_FOUND);
            }
        }
    }

    private static Set<String> getRefsUsed(String filename, Map<String,String> lookups)
    throws IOException {
        Set<String> refs = new TreeSet<String>();
        for (String type : getTypesUsed(filename)) {
            //System.out.println(type);
            extractRefsFromType(refs, type, lookups);
        }
        return refs;
    }

    private static void printTree(String filename, Map<String,String> lookups,
                                  Set<String> visited, int level, String rt)
    throws IOException {
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
        System.out.println(filename);
        if (filename.equals(rt) || filename.equals(NOT_FOUND)) {
            return;
        }
        visited.add(filename);
        for (String key : getRefsUsed(filename, lookups)) {
            //System.out.println(key + ": " + (lookups.containsKey(key) ? lookups.get(key) : "<not present>"));
            if (!visited.contains(key)) {
                printTree(key, lookups, visited, level + 1, rt);
            }
        }
        visited.remove(filename);
    }

    public static void main(String[] args) throws IOException {
        Map<String,String> lookups = new TreeMap<String,String>();
        saveClassLookups(lookups, args[0]);
        String[] cp = args[1].split(File.pathSeparator);
        for (String cpstr : cp) {
            saveClassLookups(lookups, cpstr);
        }
        String jh = System.getProperty("java.home");
        if (jh == null || jh.isEmpty()) {
            System.err.println("ERROR: java.home is not set");
            return;
        }
        String rt = jh + "/lib/rt.jar";
        if (!new File(rt).exists()) {
            System.err.println("ERROR: rt.jar not found within java.home ("+jh+")");
            return;
        }
        saveClassLookups(lookups, rt);
        printTree(args[0], lookups, new TreeSet<String>(), 0, rt);
    }
}

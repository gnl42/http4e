package me.glindholm.plugin.http4e2.httpclient.core.util;

public class JsonUtil {

    public static String hexToChar(final String i, final String j, final String k, final String l) {
        return Character.toString((char) Integer.parseInt("" + i + j + k + l, 16));
    }

    public static String render(final String value, final boolean hubav, final String indent) {
        final StringBuilder sb = new StringBuilder();
        if (hubav) {
            sb.append(indent);
        }

        sb.append("\"");
        final int len = value.length();
        for (int i = 0; i < len; i++) {
            final char lChar = value.charAt(i);
            if (lChar == '\n') {
                sb.append("\\n");
            } else if (lChar == '\r') {
                sb.append("\\r");
            } else if (lChar == '\f') {
                sb.append("\\f");
            } else if (lChar == '\t') {
                sb.append("\\t");
            } else if (lChar == '\b') {
                sb.append("\\b");
                // else if(lChar == '/') lBuf.append("\\/");
            } else if (lChar == '\"') {
                sb.append("\\\"");
            } else if (lChar == '\\') {
                sb.append("\\\\");
            } else {
                sb.append(lChar);
            }
        }

        return sb.append("\"").toString();
    }

    public static void main(final String[] args) {
        System.out.println(render("{\"JSON\":\"Hello, World!\"}"// "[50,0,0,49,[\"hhhh\"],0,0,1,288,]"
                , false, "   "));

    }

}

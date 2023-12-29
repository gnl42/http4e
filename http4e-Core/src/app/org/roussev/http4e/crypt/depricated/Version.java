package org.roussev.http4e.crypt.depricated;

import java.util.StringTokenizer;

import org.roussev.http4e.httpclient.core.util.BaseUtils;

public class Version {

    private int major;
    private int minor;
    private int build;

    public int getBuild() {
        return build;
    }

    public void setBuild(final int build) {
        this.build = build;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(final int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(final int minor) {
        this.minor = minor;
    }

    public static Version getVersion(final String str) {
        final Version v = new Version();
        if (BaseUtils.isEmpty(str)) {
            throw new RuntimeException("Blank Version");
        }

        final StringTokenizer st = new StringTokenizer(str, ".");
        v.setMajor(Integer.parseInt(st.nextToken()));
        v.setMinor(Integer.parseInt(st.nextToken()));
        v.setBuild(Integer.parseInt(st.nextToken()));
        return v;
    }

    /**
     * majorMinorRange is in format of 1-199,0-2 major,minor
     */
    public boolean isSupported(final String majorMinorRange) {

        final StringTokenizer st = new StringTokenizer(majorMinorRange, ",");
        final Range majorRange = new Range(st.nextToken());
        final Range minorRange = new Range(st.nextToken());
        if (majorRange.isInRange(getMajor()) && minorRange.isInRange(getMinor())) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Version{" + "major=" + major + ",minor=" + minor + ",build=" + build + "}";
    }

    private static class Range {
        Range(final String range) {
            final StringTokenizer st = new StringTokenizer(range, "-");
            from = Integer.parseInt(st.nextToken());
            to = Integer.parseInt(st.nextToken());
        }

        int from;
        int to;

        boolean isInRange(final int inx) {
            return inx >= from && inx <= to;
        }
    }

    public static void main(final String[] args) {
        final Version v = new Version();
        v.setMajor(1);
        v.setMinor(11);
        v.setBuild(2);

        System.out.println(v.isSupported("1-3,0-11"));
    }

}

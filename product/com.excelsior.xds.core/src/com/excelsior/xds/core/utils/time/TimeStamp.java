package com.excelsior.xds.core.utils.time;

/**
 * Represents system time at the moment of this object creation
 * @author lion
 */
public final class TimeStamp
{
    final long time;

    public TimeStamp() {
        time = System.currentTimeMillis();
    }

    public long getTime() {
        return time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (time ^ (time >>> 32));
        return result;
    }
    
    public boolean isGreaterThan(TimeStamp ts) {
        return this.time - ts.time > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimeStamp other = (TimeStamp) obj;
        if (time != other.time)
            return false;
        return true;
    }
}

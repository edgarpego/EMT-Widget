package es.tamarit.widgetemt;

import org.junit.Assert;
import org.junit.Test;

public class SplitStrings {
    
    @Test
    public void splitString() {
        
        String str = "first";
        String parts[] = str.split(":");
        Assert.assertEquals("first", parts[0]);
        Assert.assertEquals(1, parts.length);
        
        str = "first:";
        parts = str.split(":");
        Assert.assertEquals("first", parts[0]);
        Assert.assertEquals(1, parts.length);
        
        str = "first:second";
        parts = str.split(":");
        Assert.assertEquals("first", parts[0]);
        Assert.assertEquals("second", parts[1]);
        Assert.assertEquals(2, parts.length);
        
        str = "a1:a2;b1:b2";
        parts = str.split(";");
        Assert.assertEquals("a1:a2", parts[0]);
        Assert.assertEquals("b1:b2", parts[1]);
        Assert.assertEquals(2, parts.length);
        
        String partA[] = parts[0].split(":");
        Assert.assertEquals("a1", partA[0]);
        Assert.assertEquals("a2", partA[1]);
    }
    
}

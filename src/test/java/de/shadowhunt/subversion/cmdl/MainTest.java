package de.shadowhunt.subversion.cmdl;

import org.junit.Assert;
import org.junit.Test;

public class MainTest {

    @Test
    public void noArgumentTest() throws Exception {
        final boolean success = Main.delegate(AbstractCommandIT.TEST_OUT, AbstractCommandIT.TEST_ERR);
        Assert.assertTrue("must succeed", success);
    }

    @Test
    public void unknownCommandTest() throws Exception {
        final boolean success = Main.delegate(AbstractCommandIT.TEST_OUT, AbstractCommandIT.TEST_ERR, "unknown");
        Assert.assertFalse("must not succeed", success);
    }
}

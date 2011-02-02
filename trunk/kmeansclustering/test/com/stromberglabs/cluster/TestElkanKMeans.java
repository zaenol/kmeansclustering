package com.stromberglabs.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.Test;

/**
 * @author clarkus_dormanus
 *
 */
public class TestElkanKMeans
{
    public static Random r = new Random(new Date().getTime());

    public class MyClusterable implements Clusterable
    {
        public float x, y;

        @Override
        public float[] getLocation()
        {
            float[] loc = new float[] { x, y };
            return loc;
        }
    }

    public MyClusterable getRandomClusterable()
    {
        MyClusterable c = new MyClusterable();
        c.x = r.nextFloat();
        c.x = r.nextFloat();
        return c;
    }

    @Test
    public void testElkanWithListOfMyClusterable()
    {
        List<MyClusterable> clusterList = new ArrayList<MyClusterable>();
        for (int ii = 0; ii < 100; ii++)
        {
            clusterList.add(getRandomClusterable());
        }

        ElkanKMeansClusterer clusterer = new ElkanKMeansClusterer();

        // The following line does not compile
        clusterer.cluster(clusterList, 10);

        for (MyClusterable m : clusterList)
        {
            // Do something requiring m to be a MyClusterable, vice a
            // Clusterable.  This is why we want clusterList to be
            // a List<MyClusterable>
        }
    }

    @Test
    public void testElkanWithListOfClusterable()
    {
        List<Clusterable> clusterList = new ArrayList<Clusterable>();
        for (int ii = 0; ii < 100; ii++)
        {
            clusterList.add(getRandomClusterable());
        }

        ElkanKMeansClusterer clusterer = new ElkanKMeansClusterer();

        // The following line <b>does</b> compile
        clusterer.cluster(clusterList, 10);
    }
}
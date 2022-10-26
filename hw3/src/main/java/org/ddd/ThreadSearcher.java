package org.ddd;

import org.apache.lucene.search.IndexSearcher;

class ThreadSearcher extends Thread {
    private IndexSearcher searcher;

    public ThreadSearcher(IndexSearcher searcher) {
        this.searcher = searcher;
    }


    @Override
    public void run() {

    }

}
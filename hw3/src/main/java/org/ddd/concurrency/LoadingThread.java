package org.ddd.concurrency;

public class LoadingThread extends Thread{

    private String[] animation;
    private String text;

    public LoadingThread(String[] animation, String text){
        this.animation = animation;
        this.text = text;
    }

    public void run(){
        int i = 0;
        while (true) {
            System.out.print("\r" + text + this.animation[i]);
            System.out.flush();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                return;
            }
            i++;
            i = i % this.animation.length;
        }
    }
}

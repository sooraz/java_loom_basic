package base;


public class Base {
    public static void main(String[] args) throws InterruptedException {
    	System.out.println("main start");
    	Runnable runnable = ()->{
    		for (int i = 0; i < 10; i++) {
			System.out.println("thread print ::"+Thread.currentThread() +" i::"+i);
		}};
    	Thread t1 = Thread.ofVirtual().unstarted(runnable);
    	t1.start();
//    	t1.join();
    	Thread t2 = Thread.ofVirtual().unstarted(runnable);
    	t2.start();
//    	t2.join();
    	Thread t3 = Thread.ofVirtual().unstarted(runnable);
    	t3.start();
//    	t3.join();
    	Thread t4 = Thread.ofVirtual().unstarted(runnable);
    	t4.start();
//    	t4.join();
    	t1.join();
    	t2.join();
    	t3.join();
    	t4.join();

    	System.out.println("main end");
    }
}

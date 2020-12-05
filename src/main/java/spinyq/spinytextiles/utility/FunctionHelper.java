package spinyq.spinytextiles.utility;

public class FunctionHelper {

	public static class Result extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8937563127891804883L;
		
		private Object object;

		public Result(Object object) {
			this.object = object;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getResult(Runnable runnable) {
		try {
			runnable.run();
			return null;
		} catch(Result result) {
			return (T) result.object;
		}
	}
	
}

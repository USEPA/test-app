package QSAR.validation2;

import java.text.DecimalFormat;

/**
 * Class to calculate F-statistic and T-statistic
 * Code taken from numerical recipes in C
 * 
 * @author TMARTI02
 *
 */

public class Statistics {

	/**
	 * @param args
	 */
	
	private double betai(double a, double b, double x) {
		double bt;
		
		if (x<0.0 || x>1.0) {
//			System.out.println(a+"\t"+b+"\t"+x);
//			System.out.println("bad x");
			return -9999;
		}
		
		if (x==0.0 || x==1.0) bt=0;
		else 
			bt=Math.exp(gammln(a+b)-gammln(a)-gammln(b)+a*Math.log(x)+b*Math.log(1-x));

		if (x<(a+1.0)/(a+b+2.0))
			return bt*betacf(a,b,x)/a;
		else
			return 1.0-bt*betacf(b,a,1.0-x)/b;
		
		
	}
	private double gammln(double xx) {

		double x, y, tmp, ser;
		double[] cof = { 76.18009172947146, -86.50532032941677,
				24.01409828083091, -1.231739572450155, 0.1208650973886179e-2,
				-0.5395239384953e-5 };

		y = x = xx;
		tmp = x + 5.5;
		tmp -= (x + 0.5) * Math.log(tmp);
		ser = 1.000000000190015;

		for (int j = 0; j <= 5; j++)
			ser += cof[j] / ++y;
		return -tmp + Math.log(2.5066282746310005 * ser / x);
		
	}
	
	private double betacf (double a,double b,double x) {
		
		int m,m2;
		double aa,c,d,del,h,qab,qam,qap;
		double FPMIN=1.0e-30;
		double EPS=3.0e-7;
		int MAXIT=100;
		qab=a+b;
		qap=a+1.0;
		qam=a-1.0;
		c=1.0;
		d=1.0-qab*x/qap;		
		if (Math.abs(d)<FPMIN) d=FPMIN;
		d=1.0/d;
		h=d;
		for (m=1;m<=MAXIT;m++) {
			m2=2*m;
			aa=m*(b-m)*x/((qam+m2)*(a+m2));
			d=1.0+aa*d;
			if (Math.abs(d)<FPMIN) d=FPMIN;
			c=1.0+aa/c;
			if (Math.abs(c)<FPMIN) c=FPMIN;
			d=1.0/d;
			h*=d*c;
			aa=-(a+m)*(qab+m)*x/((a+m2)*(qap+m2));
			d=1.0+aa*d;
			if (Math.abs(d)<FPMIN) d=FPMIN;
			c=1.0+aa/c;
			if (Math.abs(c)<FPMIN) c=FPMIN;
			d=1.0/d;
			del=d*c;
			h*=del;
			if (Math.abs(del-1.0)<EPS) break;			
		}
		if (m>MAXIT) {
			System.out.println("maximum iterations exceeded");
			return -1;
		}
		return h;		
		
	}
	
	/**
	 * Calculates t-statistic 
	 * @param A
	 * @param v
	 * @return
	 */
	public double tstat(double A, double v) {
		double tstat=1.00;
		
//		A=1.0-(1.0-A)/2.0;
		A=2*A-1.0;
		int counter=0;
		
		while (true) {//iterate for t-statistic- stop when get sign change:
			counter++;
			
			tstat+=0.001;
			double Atvcalc=this.Atv(tstat,v);
			
			
//			System.out.println(tstat+"\t"+v+"\t"+A+"\t"+Atvcalc);
			if (Atvcalc>A) break;
			
//			if (counter%1000==0)
//				System.out.println(A+"\t"+Atvcalc+"\t"+v);
			
			if (counter>10000) {
				System.out.println(A+"\t"+v+"\t"+Atvcalc+"\t"+v+"\tCant find tstat!!!");
				break;
			}			
		}
		
		return tstat;
		
		
	}
	
	
	/**
	 * Calcs f-statistic
	 * @param A
	 * @param df1 degrees of freedom listed horizontally in Table B.4
	 * @param df2 degrees of freedom listed verically in Table B.4
	 * @return
	 */
	public double fstat(double A,double df1,double df2) {
		
		double fstat=0.00;
		
//		A=2*A-1.0;
		double incr=0.1;
		
		while (true) {//iterate for t-statistic- stop when get sign change:
			fstat+=incr;
			
			//speed things up:
			if (fstat>100) incr=1;
			if (fstat>1000) incr=10;
			if (fstat>10000) incr=100;
			if (fstat>100000) incr=1000;
			
			double Prob=this.CalcProb(fstat,df1,df2);
			
//			System.out.println(fstat+"\t"+A+"\t"+Prob);
			if (Prob>A) break;
			
		}
		
		return fstat;
		
		
	}
	
	public double CalcProb(double f,double df1,double df2) {
		
		double prob=0;
		
		double k=df2/(df2+df1*f);
		prob=1-betai(df2/2.0,df1/2.0,k);
		
//		double prob=2.0*betai(0.5*df2,0.5*df1,df2/(df2+df1*f));
//		if (prob>1.0) prob=2.0-prob;
		return prob;
		
	}
	
	
	private double Atv(double t,double v) {
		double x=v/(v+t*t);
		double a=v/2.0;
		double b=1.0/2.0;
		
		double betai=betai(a,b,x);
						
		if (betai==-9999) {			
			return -9999;
		}
		return 1.0-betai;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Statistics s=new Statistics();
		
		DecimalFormat df=new DecimalFormat("0.000");
		double A=0.975;

		
		
		//println column of table B.2
		for (int v=1;v<=28;v++) {
			System.out.println(v+"\t"+df.format(s.tstat(A,v)));	
		}
		
		
		System.out.println("");
		
		//print row of table B.4
		for (int df1=1;df1<=9;df1++) {
			System.out.println(df1+"\t"+df.format(s.fstat(A,df1,1.0)));
		}
		System.out.println("\t df1=3 df2 =17 A= 0.99 f pvalue=" +df.format(s.CalcProb(0.99,3.0,17.0)));
		System.out.println("\t df1=3 df2 =17 A= 0.49 f pvalue=" +df.format(s.CalcProb(0.49,3.0,17.0)));
		System.out.println("\t tstatistics for alpha =0.995 df1 =20 "+df.format(s.tstat(0.995,20.0)));
//		System.out.println(s.Atv(63.657,1));
	}

}

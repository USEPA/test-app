package ToxPredictor.Application.GUI.Miscellaneous;

public class ConfidenceInterval {
	
	double[] t05 = { 0, 6.314, 2.92, 2.353, 2.132, 2.015, 1.943, 1.895, 1.86,
			1.833, 1.812, 1.796, 1.782, 1.771, 1.761, 1.753, 1.746, 1.74,
			1.734, 1.729, 1.725, 1.721, 1.717, 1.714, 1.711, 1.708, 1.706,
			1.703, 1.701, 1.699, 1.697, 1.684, 1.671, 1.658, 1.645 };

	double[] t025 = { 0, 12.706, 4.303, 3.182, 2.776, 2.571, 2.447, 2.365,
			2.306, 2.262, 2.228, 2.201, 2.179, 2.16, 2.145, 2.131, 2.12, 2.11,
			2.101, 2.093, 2.086, 2.08, 2.074, 2.069, 2.064, 2.06, 2.056, 2.052,
			2.048, 2.045, 2.042, 2.021, 2, 1.98, 1.96 };
	
	double [][] X={			
			{0,0,0,0},			
			{0,1,1.63669,2.1297},		
			{0,1,0.871949,1.83057},
			{0,1,0.387543,1.39128},
			{0,1,1.08043,1.68382},
			{0,1,1.46518,0.216832},
			{0,1,1.30502,0.0315228},
			{0,1,0.988792,0.556235},
			{0,1,1.08043,1.66567},
			{0,1,0.890004,2.35458}
	};
	
	
	double [][]x0 = { 			
			{0,0},
			{0,1},
			{0,0.651537},
			{0,0.5455}};  // NP+1 x 1 matrix (excluding zero rows&cols)

		
	double [][] Y={
			{0,0},			
			{0,2.12927},
			{0,2.95768},
			{0,4.10768},
			{0,3.15631},
			{0,1.29611},
			{0,1.74967},
			{0,2.39474},
			{0,3.06916},
			{0,3.62859}};
	
	void Transpose(double [][]XDATA, double [][] XT,int NUMXCOL, int N) {
		
		
		try {
			for (int i=1;i<=N;i++) {
				for (int j=1;j<=NUMXCOL;j++) {
					XT[j][i] = XDATA[i][j];
				}
			}
		}catch (Exception e) {
			System.out.println(e);
		}	    	   
	}
	
	void MatrixProduct(double [][] M1, double [][]M2, double [][]PROD, int ROWSM1, int COLSM1, int COLSM2) {
	 // note: size of matrix product: num rows in matrix 1 and num cols in  matrix 2
		for (int i=1;i<=ROWSM1;i++) {
			for (int j=1;j<=COLSM2;j++) {
				double SUM = 0;
				
				for (int k=1;k<=COLSM1;k++) {
					SUM = SUM + M1[i][k] * M2[k][j];                
				}				
				PROD[i][j] = SUM;
			}
		}
		
	}
	
	void Inverse(double [][]M,double [][] InvM,int N) {
				
		double [] B=new double [N+1];
		int [] INDX=new int [N+1];
		
		try {
		for (int I=1;I<=N;I++) {
			for (int J=1;J<=N;J++) {
				InvM[I][J] = 0;
			}
			InvM[I][I] = 1;
		}
		
		this.LUDCMP(M, N, INDX);
		
		for (int J=1;J<=N;J++) {		    
			for (int I=1;I<=N;I++) {
				B[I] = InvM[I][J];
			}
			this.LUBKSB(M, N, INDX, B);
			
			for (int I=1;I<=N;I++) {
				InvM[I][J] = B[I];
			}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // end Inverse
	
	void LUDCMP(double [][]A, int N,int [] INDX) {
		double TINY,AAMAX, SUM, DUM;
		int IMAX=0,D;
		double [] VV=new double [N+1];
		
		
		TINY = 1E-20;
		D = 1;
		
		for (int I=1;I<=N;I++) {
			AAMAX = 0;
			
			for (int J=1;J<=N;J++) {		  
				if (Math.abs(A[I][J]) > AAMAX) AAMAX = Math.abs(A[I][J]);
			}
			
			if (AAMAX == 0) System.out.println("SINGULAR MATRIX");
			VV[I] = 1 / AAMAX;
		}
		
		for (int J=1;J<=N;J++) {
			for (int I=1;I<=J-1;I++) {			
				SUM = A[I][J];
				
				for (int K=1;K<=I-1;K++) {				
					SUM = SUM - A[I][K] * A[K][J];
				}
				A[I][J] = SUM;
			}
			
			AAMAX = 0;
			
			for (int I=J;I<=N;I++) {
				
				SUM = A[I][J];
				
				for (int K=1;K<=J-1;K++) {            
					SUM = SUM - A[I][K] * A[K][J];
				}
				A[I][J] = SUM;
				DUM = VV[I] * Math.abs(SUM);
				if ( DUM > AAMAX) {
					IMAX = I;
					AAMAX = DUM;
				}
			}// end I Loop
			if (J != IMAX) {
				for (int K=1;K<=N;K++) {        	
					DUM = A[IMAX][K];
					A[IMAX][K] = A[J][K];
					A[J][K] = DUM;
				}// end K loop
				D = -D;
				VV[IMAX] = VV[J];
			} // end if
			
			INDX[J] = IMAX;
			if (A[J][J] == 0) A[J][J] = TINY;
			if (J != N) {
				DUM = 1 / A[J][J];
				
				for (int I=J+1;I<=N;I++ ) {            	
					A[I][J] = A[I][J] * DUM;
				}
			} // end if
		}// end outer J loop
		
	}
	
	
	void LUBKSB(double [][]A, int N, int [] INDX, double []B) {
		// from Numerical Recipes book by Press
		double SUM;//Dim I%, J%, II%, LL%, SUM#
		int II,LL;
		II = 0;
		
		for (int I=1;I<=N;I++) {    
			LL = INDX[I];
			SUM = B[LL];
			B[LL] = B[I];
			if (II != 0) {
				for (int J=II;J<=I-1;J++) {
					SUM = SUM - A[I][J] * B[J];
				}
			} else if (SUM != 0) {
				II = I;
			}
			
			B[I] = SUM;
		}
		
		for (int I=N;I>=1;I--) {
			
			SUM = B[I];
			if (I < N) {
				for (int J=I+1;J<=N;J++) {
					SUM = SUM - A[I][J] * B[J];
				}
			}
			B[I] = SUM / A[I][I];
		}
		
	}
	
	void CalculateYcalc(double [] Ycalc, int N,int NP,double [][] XVECLS,double [][]X) {
		for (int i=1;i<=N;i++) {
			Ycalc[i]=0;
						
			for (int j=1;j<=NP;j++) {
				Ycalc[i]+=XVECLS[j][1]*X[i][j];
			}
						
		}
	}
	
	
	
	double CalculateMeanSquareDeviation(int N,int NP,double []Ycalc,double [][]Y) {
		double MSD=0;
		double Resid=0;
		
		for (int i=1;i<=N;i++) {			
			Resid=Y[i][1]-Ycalc[i];
			MSD+=Resid*Resid;
						
		}
		MSD/=(N-NP);
		return MSD;
		
	}
	
	double Interpolate(double X1,double Y1,double X2,double Y2,double X3) {
		double I=0;// interpolated value
		
		I=(X3-X1)*(Y2-Y1)/(X2-X1)+Y1;
				
		return I;
		
	}
	
	
	
	
	
	void CalculateConfidenceInterval() {
		//TODO: recode using Jama code since LU Decomposition from Numerical Recipes book is not open license
		//TODO: possibly get t-test values from http://jakarta.apache.org/commons/math/
		
		int N=9; // number of chemicals in cluster
		int NP=3; // number of parameters
		double MSD,CI90,CI95;
		double Tstat90=0,Tstat95=0;
		
		double [][]XT=new double [NP+1][N+1];
		double [][]XTX=new double [NP+1][NP+1];
		double [][]invXTX=new double [NP+1][NP+1];
		double [][]invXTX_XT=new double [NP+1][N+1];
		double [][]XVECLS=new double [NP+1][2];		
		double [][] x0T=new double [1+1][NP+1];
		double [][] x0T_intXTX=new double [1+1][NP+1];
		double [][] OnePlus_x0T_invXTX=new double [1+1][NP+1];
		double [][] OnePlus_x0T_invXTX_x0=new double [1+1][1+1];
		
		double []Ycalc=new double [N+1];
		double []Yexp=new double [N+1];
		
		for (int i=1;i<=N;i++) {			
			Yexp[i]=Y[i][1]; 
		}
								
		this.Transpose(X,XT,NP,N);
		this.MatrixProduct(XT,X,XTX,NP,N,NP);
 
		this.Inverse(XTX,invXTX,NP);
									
		// finish linear regression calcs:		
		this.MatrixProduct(invXTX,XT,invXTX_XT,NP,NP,N); // calculates invXTX_XT
		this.MatrixProduct(invXTX_XT,Y,XVECLS,NP,N,1); // calculates invXTX_XT_Y = parameter vector = least square solution
		
		
		System.out.println("\nParams:");
		for (int i=1;i<=NP;i++) {
			System.out.println(XVECLS[i][1]);
		}
		System.out.println("");
		
		this.CalculateYcalc(Ycalc,N,NP,XVECLS,X);
				
		MSD=this.CalculateMeanSquareDeviation(N,NP,Ycalc,Y);
	    System.out.println("MSD="+MSD);
		
	    this.Transpose(x0,x0T,1,NP);
	    
	    this.MatrixProduct(x0T,invXTX,x0T_intXTX,1,NP,NP);
	    
	    for (int i=1;i<=1;i++) {
	    	for (int j=1;j<=NP;j++) {
	    		OnePlus_x0T_invXTX[i][j]=1+x0T_intXTX[i][j];
	    	}	    	
	    }
	    
	    this.MatrixProduct(OnePlus_x0T_invXTX,x0,OnePlus_x0T_invXTX_x0,1,NP,1);
	    	    	    
	    int DOF=N-NP;  // dont need to subtract 1 since constant is included in NP
	    	    	   
	    if (DOF<=30) {
	    	Tstat90=this.t05[DOF];
	    	Tstat95=this.t025[DOF];
	    } else if (DOF>30 && DOF <=40){
	    	Tstat90=this.Interpolate(30,this.t05[30],40,this.t05[31],DOF);
	    	Tstat95=this.Interpolate(30,this.t025[30],40,this.t025[31],DOF);	    	
	    } else if (DOF>40 && DOF <=60) {	
	    	Tstat90=this.Interpolate(40,this.t05[31],60,this.t05[32],DOF);
	    	Tstat95=this.Interpolate(40,this.t025[31],60,this.t025[32],DOF);	    	
	    } else if (DOF>60 && DOF <=120) {
	    	Tstat90=this.Interpolate(60,this.t05[32],120,this.t05[33],DOF);
	    	Tstat95=this.Interpolate(60,this.t025[32],120,this.t025[33],DOF);
	    } else if (DOF > 120) {
	    	Tstat90=this.t05[34];
	    	Tstat95=this.t025[34];
	    }
	    
	    System.out.println("DOF="+DOF);
	    System.out.println("Tstat90="+Tstat90);
	    System.out.println("Tstat95="+Tstat95);
	    
	    CI90=Tstat90*Math.sqrt(MSD*OnePlus_x0T_invXTX_x0[1][1]);
	    CI95=Tstat95*Math.sqrt(MSD*OnePlus_x0T_invXTX_x0[1][1]);
	    
	    System.out.println("CI90 = "+CI90);
	    System.out.println("CI95 = "+CI95);    
	    
	    fraChart fc=new fraChart();
	    	    	    
	    fc.jlChart.Y1=Ycalc;
	    fc.jlChart.X=Yexp;
	    	    
	    fc.setVisible(true);
	    
		
	}
	
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		ConfidenceInterval ci=new ConfidenceInterval();
		ci.CalculateConfidenceInterval();
						
	}
	
}

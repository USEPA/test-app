package ToxPredictor.MyDescriptors;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.Ring;
import org.openscience.cdk.interfaces.IAtom;
//import org.openscience.cdk.*;
//import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRingSet;

public class MLOGP {
	IAtomContainer m;
	DescriptorData dd;
	IRingSet rs;
	String[] Fragment; // estate fragments for each atom
	AtomicProperties ap;

	private double CX, NO, PRX, UB, HB, POL, AMP, ALK, RNG, QN, NO2, NCS, BLM;

	public MLOGP() {

		try {
			ap = AtomicProperties.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void Calculate(IAtomContainer m, DescriptorData dd, String[] Fragment, IRingSet rs) {
		this.m = m;

		this.dd = dd;
		this.Fragment = Fragment;
		this.rs = rs;

		CX = 0;
		NO = 0;
		PRX = 0;
		UB = 0;
		HB = 0;
		POL = 0;
		AMP = 0;
		ALK = 0;
		RNG = 0;
		QN = 0;
		NO2 = 0;
		NCS = 0;
		BLM = 0;

		this.CalculateCX();
		this.CalculateNO();

		this.CalculatePRX();
		this.CalculateALK();
		this.CalculateUB_NO2();
		this.CalculateRNG();
		this.CalculateBLM();
		this.CalculatePOL();
		this.CalculateNCS();
		this.CalculateQN();
		this.CalculateAMP();
		this.CalculateHB();

		dd.MLOGP = 1.244 * Math.pow(CX, 0.6) - 1.017 * Math.pow(NO, 0.9) + 0.406 * PRX;
		dd.MLOGP += -0.145 * Math.pow(UB, 0.8) + 0.511 * HB + 0.268 * POL - 2.215 * AMP;
		dd.MLOGP += 0.912 * ALK - 0.392 * RNG - 3.684 * QN + 0.474 * NO2;
		dd.MLOGP += 1.582 * NCS + 0.773 * BLM - 1.041;

//		System.out.println("CX = "+CX);
//		System.out.println("NO = "+NO);
//		System.out.println("PRX = "+PRX);
//		System.out.println("UB = "+UB);
//		System.out.println("HB = "+HB);
//		System.out.println("POL = "+POL);
//		System.out.println("AMP = "+AMP);
//		System.out.println("ALK = "+ALK);
//		System.out.println("RNG = "+RNG);
//		System.out.println("QN = "+QN);
//		System.out.println("NO2 = "+NO2);
//		System.out.println("NCS = "+NCS);
//		System.out.println("BLM = "+BLM);
//
//		System.out.println("\nMLOGP="+dd.MLOGP);

	}

	private void CalculateCX() {

		this.CX = dd.nC * 1.0 + dd.nF * 0.5 + dd.nCL * 1.0 + dd.nBR * 1.5 + dd.nI * 2.0;
	}

	private void CalculateALK() {
		this.ALK = 0;

		for (int i = 0; i <= m.getAtomCount() - 1; i++) {
			if (!m.getAtom(i).getSymbol().equals("C") && !m.getAtom(i).getSymbol().equals("H")) {
				return;
			}
		}

		if (dd.nDB <= 1 && dd.nTB == 0) {
			this.ALK = 1;
		}

	}

	private void CalculateAMP() {
		this.AMP = 0;

		// alpha amino acid: H2N-CH(R)-COOH
		for (int i = 0; i <= m.getAtomCount() - 1; i++) {

			boolean HaveNH2 = false;
			boolean HaveCdO = false;
			boolean HaveCsOH = false;

			if (!(Fragment[i] instanceof String))
				continue;

			if (!Fragment[i].equals("SsssCH") && !Fragment[i].equals("SssCH2")) {
				continue;
			}

//			System.out.println("heress");

			IAtom ai = m.getAtom(i);

			List<IAtom> ca = m.getConnectedAtomsList(ai);

			for (IAtom caj : ca) {

				if (!(Fragment[m.indexOf(caj)] instanceof String)) {
					continue;
				}

				if (Fragment[m.indexOf(caj)].equals("SsNH2")) {
					HaveNH2 = true;
				}

				if (Fragment[m.indexOf(caj)].equals("SdssC")) {
					List<IAtom> ca2 = m.getConnectedAtomsList(caj);

					for (IAtom ca2k : ca2) {
						if (!(Fragment[m.indexOf(ca2k)] instanceof String)) {
							continue;
						}
						if (Fragment[m.indexOf(ca2k)].equals("SdO")) {
							HaveCdO = true;
						} else if (Fragment[m.indexOf(ca2k)].equals("SsOH")) {
							HaveCsOH = true;
						}
					}

				}

			} // end connected atom loop

			if (HaveNH2 && HaveCdO && HaveCsOH) {
				this.AMP += 1.0;
				return;
			}

		} // end atom i loop

		/*
		 * // amino benzoic acid
		 * 
		 * // look for NH2 and COOH attached to same benzene ring
		 * 
		 * 
		 * for (int i=0;i<=rs.size()-1;i++) { Ring r=(Ring)rs.get(i);
		 * 
		 * boolean HaveACNH2=false; boolean HaveACCOOH=false;
		 * 
		 * if (r.getAtomCount()!=6) continue;
		 * 
		 * for (int j=0;j<=r.getAtomCount()-1;j++) { Atom []
		 * ca=m.getConnectedAtoms(r.getAtom(j));
		 * 
		 * for (int k=0;k<ca.size();k++) { if (!r.contains(((IAtom)ca.get(k)))) { if
		 * (Fragment[m.indexOf(((IAtom)ca.get(k)))].equals("SsNH2")) { HaveACNH2=true; }
		 * }
		 * 
		 * }
		 * 
		 * } // end over loop over atoms in ring System.out.println(HaveACNH2); } // end
		 * rs loop
		 * 
		 * 
		 */

		// pyridine carboxylic acid

		for (int i = 0; i <= rs.getAtomContainerCount() - 1; i++) {
			Ring r = (Ring) rs.getAtomContainer(i);

			boolean HaveAN = false;
			boolean HaveCdO = false;
			boolean HaveCsOH = false;
			boolean HaveACCOOH = false;

			if (r.getAtomCount() != 6)
				continue;

			for (int j = 0; j <= r.getAtomCount() - 1; j++) {

				if (Fragment[m.indexOf(r.getAtom(j))].equals("SaaN")) {
					HaveAN = true;
				}

				List<IAtom> ca = m.getConnectedAtomsList(r.getAtom(j));

				for (IAtom cak : ca) {
					if (!r.contains(cak)) {
						if (Fragment[m.indexOf(cak)].equals("SdssC")) {
							List<IAtom> ca2 = m.getConnectedAtomsList(cak);

							for (IAtom ca2l : ca2) {
								if (Fragment[m.indexOf(ca2l)].equals("SdO")) {
									HaveCdO = true;
								} else if (Fragment[m.indexOf(ca2l)].equals("SsOH")) {
									HaveCsOH = true;
								}
							}
							if (HaveCdO && HaveCsOH)
								HaveACCOOH = true;
						}
					}

				}
				if (HaveACCOOH && HaveAN) {
					this.AMP += 0.5;
					return;
				}

			} // end over loop over atoms in ring

		} // end rs loop

	}

	private void CalculateBLM() {
		// find b-lactam ring

		iloop: for (int i = 0; i <= rs.getAtomContainerCount() - 1; i++) {
			Ring r = (Ring) rs.getAtomContainer(i);
			if (r.getAtomCount() == 4) {

				boolean HaveCdO = false;
				boolean HaveN = false;
				boolean HaveC1 = false;
				boolean HaveC2 = false;

				int CdO_atomnumber = -1;
				int N_atomnumber = -1;
				int C1_atomnumber = -1;
				int C2_atomnumber = -1;

				jloop: for (int j = 0; j <= r.getAtomCount() - 1; j++) {

					if (r.getAtom(j).getSymbol().equals("C")) {

						List<IAtom> ca = m.getConnectedAtomsList(r.getAtom(j));

						for (IAtom cak : ca) {
							if (cak.getSymbol().equals("O")
									&& m.getBond(r.getAtom(j), cak).getOrder().equals(IBond.Order.DOUBLE)
									&& !r.contains(cak)) {
								HaveCdO = true;
								CdO_atomnumber = m.indexOf(r.getAtom(j));
							}
						}

						if (HaveCdO) {
							for (IAtom cak : ca) {
								if (cak.getSymbol().equals("N")
										&& m.getBond(r.getAtom(j), cak).getOrder().equals(IBond.Order.SINGLE)
										&& r.contains(cak)) {
									HaveN = true;
									N_atomnumber = m.indexOf(cak);
								}
							}
						}

						if (HaveCdO && HaveN) {
							for (IAtom cak : ca) {
								if (cak.getSymbol().equals("C")
										&& m.getBond(r.getAtom(j), cak).getOrder().equals(IBond.Order.SINGLE)
										&& r.contains((cak))) {
									C1_atomnumber = m.indexOf(cak);
									if (Fragment[m.indexOf(cak)].equals("SsssCH")) // is this necessary (ie too
																					// restrictive?)
										HaveC1 = true;
								}

							}
						}

						if (HaveCdO && HaveN && HaveC1) {

							List<IAtom> ca2 = m.getConnectedAtomsList(m.getAtom(C1_atomnumber));

							for (IAtom ca2l : ca2) {

								if (r.contains(ca2l) && m.indexOf(ca2l) != CdO_atomnumber) {

									if (Fragment[m.indexOf(ca2l)].equals("SsssCH")) { // is this necessary (ie too
																						// restrictive?)
										C2_atomnumber = m.indexOf(ca2l);
										HaveC2 = true;
									}
								}
							}
						}

//						System.out.println(CdO_atomnumber);
//						System.out.println(N_atomnumber);
//						System.out.println(C1_atomnumber);
//						System.out.println(C2_atomnumber);

					}

				} // end loop over atoms in ring

				if (HaveN && HaveCdO && HaveC1 && HaveC2) {
					this.BLM = 1;
					return;
				}

			} // end if ringsize =4

		} // end loop over ringset elements
	}

	private void CalculateNO() {
		this.NO = dd.nN * 1.0 + dd.nO;
	}

	private void CalculateUB_NO2() {

		this.UB = 0;
		for (int i = 0; i < m.getBondCount(); i++) {
			if (m.getBond(i).getOrder().numeric() > 1) {
				this.UB++;
			}
		}

		// this.UB=dd.nDB+dd.nTB;

		// xxxiii. ACNO2
		// xxxiii. NO2
		// xxxiii. ONO2

		this.NO2 = 0;

		for (int i = 0; i <= m.getAtomCount() - 1; i++) {

			if (!Fragment[i].equals("SssdNp"))
				continue;

			boolean HaveSsOm = false;
			boolean HaveSdO = false;
//			boolean Ar = false;

			List<IAtom> ca = m.getConnectedAtomsList(m.getAtom(i));

			for (IAtom caj : ca) {
				if (Fragment[m.indexOf(caj)].equals("SsOm")) {
					HaveSsOm = true;
				} else if (Fragment[m.indexOf(caj)].equals("SdO")) {
					HaveSdO = true;
				}
			}

			if (HaveSsOm && HaveSdO) {
				NO2++;
			}

		}

		// try {
//		String strval=(String)dd.FragmentList.get("ACNO2");
//        int NO2count=Integer.parseInt(strval);
//        
//		strval=(String)dd.FragmentList.get("NO2");
//        NO2count+=Integer.parseInt(strval);
//
//		strval=(String)dd.FragmentList.get("ONO2");
//        NO2count+=Integer.parseInt(strval);

		// this.NO2=NO2count;
		this.UB -= this.NO2;

//		} catch (Exception e) {
//	        this.NO2=-1;
//	        this.UB-=-1;
//	        e.printStackTrace();
//			
//		}

	}

	private void CalculatePOL() {

		for (int i = 0; i <= m.getAtomCount() - 1; i++) {
			IAtom ai = m.getAtom(i);

			if (ai.getFlag(CDKConstants.ISAROMATIC)) {
				continue;
			}

			int AromaticRingAttachmentCount = 0;
			boolean IsElectronegative = false;
			boolean AttachedToElectronegative = false;
			boolean HaveCC = false;

			List<IAtom> ca = m.getConnectedAtomsList(m.getAtom(i));

			for (IAtom caj : ca) {
				if (caj.getFlag(CDKConstants.ISAROMATIC)) { // next atom is aromatic
					AromaticRingAttachmentCount++;

				} else {

					if (m.getBond(ai, caj).getOrder().numeric() > 0 && ai.getSymbol().equals("C")
							&& caj.getSymbol().equals("C")) {
						HaveCC = true;
					}

					if (ap.GetNormalizedElectronegativity(caj.getSymbol()) > 1) {
						if (m.getBond(caj, m.getAtom(i)).getOrder().numeric() > 1) {
							AttachedToElectronegative = true;
						} else if (m.getBond(caj, m.getAtom(i)).getOrder().equals(IBond.Order.SINGLE)) {
							String f = Fragment[m.indexOf(caj)];
							if (f.equals("SsBr") || f.equals("SsCl") || f.equals("SsF") || f.equals("SsNH2")
									|| f.equals("SsOH")) {
								AttachedToElectronegative = true;
							}
						}
					}
				}
			}

			if (ap.GetNormalizedElectronegativity(m.getAtom(i).getSymbol()) > 1) {
				IsElectronegative = true;
			}
			if (AromaticRingAttachmentCount > 0 && (IsElectronegative || AttachedToElectronegative)) {
//				if (!HaveCC) 
				this.POL += AromaticRingAttachmentCount;
			}

		}

		if (this.POL > 4)
			this.POL = 4;
	}

	private void CalculatePRX() {
		this.PRX = 0;
		// first check for N/O

		List<String> al = new ArrayList<>();

		for (int i = 0; i <= m.getAtomCount() - 1; i++) {
			IAtom ai = m.getAtom(i);

			if (!ai.getSymbol().equals("N") && !ai.getSymbol().equals("O"))
				continue;

			List<IAtom> ca = m.getConnectedAtomsList(ai);
//			System.out.println(i+"\t"+ai.getSymbol());

			for (IAtom caj : ca) {

				if (caj.getSymbol().equals("O") || caj.getSymbol().equals("N")) {

					String pair;
					if (i < m.indexOf(caj)) {
						pair = i + "\t" + m.indexOf(caj);
					} else {
						pair = m.indexOf(caj) + "\t" + i;
					}

					if (!checkformatches(al, pair)) {
						PRX += 2;
					}

				}
			}
		}

		for (int i = 0; i <= m.getAtomCount() - 1; i++) {
			IAtom ai = m.getAtom(i);

			List<IAtom> ca = m.getConnectedAtomsList(ai);

			if (!ai.getSymbol().equals("N") && !ai.getSymbol().equals("O"))
				continue;

//			System.out.println(i+"\t"+ai.getSymbol());

			for (IAtom caj : ca) {

				if (!caj.getSymbol().equals("C") && !caj.getSymbol().equals("S") && !caj.getSymbol().equals("P"))
					continue;

				List<IAtom> ca2 = m.getConnectedAtomsList(caj);

				for (IAtom ca2k : ca2) {
					if (m.indexOf(ca2k) == i)
						continue;

					if (!ca2k.getSymbol().equals("N") && !ca2k.getSymbol().equals("O"))
						continue;

					String pair;
					if (i < m.indexOf(ca2k)) {
						pair = i + "\t" + m.indexOf(ca2k);
					} else {
						pair = m.indexOf(ca2k) + "\t" + i;
					}

					if (!checkformatches(al, pair)) {
						if (ai.getSymbol().equals("O") && ca2k.getSymbol().equals("N")) {

							if (m.getBond(caj, ai).getOrder().equals(IBond.Order.DOUBLE)
									&& m.getBond(caj, ca2k).getOrder().equals(IBond.Order.SINGLE)) {

								boolean HaveCsO = false;
								for (IAtom ca2l : ca2) {
									if (m.getBond(ca2l, caj).getOrder().equals(IBond.Order.SINGLE)
											&& ca2l.getSymbol().equals("O")) {
										HaveCsO = true;
										break;
									}
								}

								if (!HaveCsO)
									PRX += 1;
							} else {
								boolean HaveCdO = false;
								for (IAtom ca2l : ca2) {
									if (m.getBond(ca2l, caj).getOrder().equals(IBond.Order.DOUBLE)
											&& ca2l.getSymbol().equals("O")) {
										HaveCdO = true;
										break;
									}
								}

								if (!HaveCdO)
									PRX += 1;
							}

						} else if (ai.getSymbol().equals("N") && ca2k.getSymbol().equals("O")) {
							if (m.getBond(caj, ai).getOrder().equals(IBond.Order.SINGLE)
									&& m.getBond(caj, ca2k).getOrder().equals(IBond.Order.DOUBLE)) {

								boolean HaveCsO = false;
								for (IAtom ca2l : ca2) {
									if (m.getBond(ca2l, caj).getOrder().equals(IBond.Order.SINGLE)
											&& ca2l.getSymbol().equals("O")) {
										HaveCsO = true;
										break;
									}
								}

								if (!HaveCsO)

									PRX += 1;
							} else {
								boolean HaveCdO = false;
								for (IAtom ca2l : ca2) {
									if (m.getBond(ca2l, caj).getOrder().equals(IBond.Order.DOUBLE)
											&& ca2l.getSymbol().equals("O")) {
										HaveCdO = true;
										break;
									}
								}

								if (!HaveCdO)
									PRX += 1;
							}

						} else if (ai.getSymbol().equals("N") && ca2k.getSymbol().equals("N")) {
							if ((m.getBond(caj, ai).getOrder().equals(IBond.Order.DOUBLE)
									&& m.getBond(caj, ca2k).getOrder().equals(IBond.Order.SINGLE))
									|| (m.getBond(caj, ai).getOrder().equals(IBond.Order.SINGLE)
											&& m.getBond(caj, ca2k).getOrder().equals(IBond.Order.DOUBLE))) {
								PRX += 2;
							} else if (m.getBond(caj, ai).getOrder().equals(IBond.Order.SINGLE)
									&& m.getBond(caj, ca2k).getOrder().equals(IBond.Order.SINGLE)) {

								// check if intervening atom is double bonded to O
								boolean HaveCdO = false;
								for (IAtom ca2l : ca2) {
									if (m.getBond(ca2l, caj).getOrder().equals(IBond.Order.DOUBLE)
											&& ca2l.getSymbol().equals("O")) {
										HaveCdO = true;
										break;
									}
								}

								if (!HaveCdO)
									PRX += 1;

							} else if (m.getBond(caj, ai).getOrder().equals(IBond.Order.DOUBLE)
									&& m.getBond(caj, ca2k).getOrder().equals(IBond.Order.DOUBLE)) {
								// ?
							}

						} else if (ai.getSymbol().equals("O") && ca2k.getSymbol().equals("O")) {

							if ((m.getBond(caj, ai).getOrder().equals(IBond.Order.DOUBLE)
									&& m.getBond(caj, ca2k).getOrder().equals(IBond.Order.SINGLE))
									|| (m.getBond(caj, ai).getOrder().equals(IBond.Order.SINGLE)
											&& m.getBond(caj, ca2k).getOrder().equals(IBond.Order.DOUBLE))) {
								PRX += 2;
							} else if (m.getBond(caj, ai).getOrder().equals(IBond.Order.SINGLE)
									&& m.getBond(caj, ca2k).getOrder().equals(IBond.Order.SINGLE)) {
//								 check if intervening atom is double bonded to O
								boolean HaveCdO = false;
								for (IAtom ca2l : ca2) {
									if (m.getBond(ca2l, caj).getOrder().equals(IBond.Order.DOUBLE)
											&& ca2l.getSymbol().equals("O")) {
										HaveCdO = true;
										break;
									}
								}

								if (!HaveCdO)
									PRX += 1;
							} else if (m.getBond(caj, ai).getOrder().equals(IBond.Order.DOUBLE)
									&& m.getBond(caj, ca2k).getOrder().equals(IBond.Order.DOUBLE)) {
								// ?
							}

						}

					}
				}

			}
		}

//		for (int i=0;i<=al.size()-1;i++) {
//			System.out.println(al.get(i));
//		}

//		this.PRX/=2.0; // avoid double counting

//		System.out.println("PRX="+this.PRX);

//		 correction for carboxamide		
//		for (int i=0;i<=m.getAtomCount()-1;i++) {
//			boolean HaveCdO=false;
//			int CountN=0;
//			
//			IAtom ai=m.getAtom(i);
//			
//			if (!ai.getSymbol().equals("C") ) continue;
//			
//			java.util.List ca=m.getConnectedAtomsList(m.getAtom(i));
//			
//			for (int j=0;j<ca.size();j++) {
//				if (((IAtom)ca.get(j)).getSymbol().equals("O")) {
//					if (m.getBond(ai,((IAtom)ca.get(j))).getOrder().equals(IBond.Order.DOUBLE)) {
//						HaveCdO=true;
//					}
//				} else if (((IAtom)ca.get(j)).getSymbol().equals("N")) {
//					CountN++;
//				}
//			}
//			if (!HaveCdO) continue;
//			//System.out.println("HaveCdO");
//			
//			this.PRX-=CountN;
//			
//			
//		}
//
////		 correction for sulfonamide
//		for (int i=0;i<=m.getAtomCount()-1;i++) {
//			int CountSdO=0;
//			boolean HaveN=false;
//			
//			IAtom ai=m.getAtom(i);
//			
//			if (!ai.getSymbol().equals("S") ) continue;
//			
//			java.util.List ca=m.getConnectedAtomsList(m.getAtom(i));
//			
//			for (int j=0;j<ca.size();j++) {
//				if (((IAtom)ca.get(j)).getSymbol().equals("O")) {
//					if (m.getBond(ai,((IAtom)ca.get(j))).getOrder().equals(IBond.Order.DOUBLE)) {
//						CountSdO++;
//					}
//				} else if (((IAtom)ca.get(j)).getSymbol().equals("N")) {
//					HaveN=true;
//				}
//			}
//			
//			if (CountSdO==2 && HaveN) {
//				this.PRX-=4.0; // TODO: find better way to correct for sulfonamides
//			}
//			
//		}

	}

	private void CalculateRNG() {

		try {

			for (int i = 0; i <= rs.getAtomContainerCount() - 1; i++) {

				Ring ir = (Ring) rs.getAtomContainer(i);

				boolean ContainsHetero = false;
				for (int j = 0; j <= ir.getAtomCount() - 1; j++) {
					if (!ir.getAtom(j).getSymbol().equals("C") && !ir.getAtom(j).getSymbol().equals("N")) {
						ContainsHetero = true;
						this.RNG = 1;
						return;
					}
				}

				if (!ir.getFlag(CDKConstants.ISAROMATIC)) {
					this.RNG = 1;
					return;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkformatches(List<String> al, String bob) {

		if (al.contains(bob))
			return true;
		else {
			al.add(bob);
			return false;
		}

	}

	private void CalculateNCS() {

		// isothiocyanato (N=C=S):
		String strval;

		this.NCS = 0;

		// thiocyanato (S-C#N):
		for (int i = 0; i <= m.getAtomCount() - 1; i++) {
			IAtom ai = m.getAtom(i);

			boolean HaveCtN = false;
			boolean HaveCsS = false;

			boolean HaveCdN = false;
			boolean HaveCdS = false;

			if (ai.getSymbol().equals("C")) {
				List<IAtom> ca = m.getConnectedAtomsList(ai);
				for (IAtom caj : ca) {
					if (caj.getSymbol().equals("N")) {
						if (m.getBond(ai, caj).getOrder().equals(IBond.Order.TRIPLE)) {
							HaveCtN = true;
						} else if (m.getBond(ai, caj).getOrder().equals(IBond.Order.DOUBLE)) {
							HaveCdN = true;
						}
					} else if (caj.getSymbol().equals("S")) {
						if (m.getBond(ai, caj).getOrder().equals(IBond.Order.SINGLE)) {
							HaveCsS = true;
						} else if (m.getBond(ai, caj).getOrder().equals(IBond.Order.DOUBLE)) {
							HaveCdS = true;
						}
					}
				}
				if (HaveCtN && HaveCsS) {
					this.NCS += 0.5;
				} else if (HaveCdN && HaveCdS) {
					this.NCS++;
				}

			}
		}

	}

	private void CalculateQN() {
		for (int i = 0; i <= m.getAtomCount() - 1; i++) {
			IAtom ai = m.getAtom(i);

			if (!(Fragment[i] instanceof String))
				continue;
			if (Fragment[i].equals("SssssNp")) {
				List<IAtom> ca = m.getConnectedAtomsList(ai);

				boolean HaveOm = false;

				for (IAtom caj : ca) {
					if (Fragment[m.indexOf(caj)].equals("SsOm")) {
						HaveOm = true;
						break;
					}
				}

				if (HaveOm) {
					this.QN += 0.5;
				} else {
					this.QN++;
				}

				return;
			} // end SssssNp if

		} // end i atom loop

	}

	private void CalculateHB() {
		for (int i = 0; i <= rs.getAtomContainerCount() - 1; i++) {
			Ring r = (Ring) rs.getAtomContainer(i);

			if (!r.getFlag(CDKConstants.ISAROMATIC))
				continue;

			for (int j = 0; j <= r.getAtomCount() - 1; j++) {

				boolean HaveACNH2 = false;
				boolean HaveACCOOH = false;
				boolean HaveACOH = false;
				boolean HaveACCO = false;

				List<IAtom> ca = m.getConnectedAtomsList(r.getAtom(j));
				// System.out.println("j="+j);

				for (IAtom cak : ca) {
					if (!r.contains(cak)) {
						String f = Fragment[m.indexOf(cak)];
						// System.out.println(f);
						if (!(f instanceof String))
							continue;

						if (f.equals("SsNH2")) {
							HaveACNH2 = true;
						} else if (f.equals("SsOH")) {
							HaveACOH = true;
						}

						if (HaveCOOH(cak))
							HaveACCOOH = true;
						if (HaveCO(cak))
							HaveACCO = true;

					} else {// find attachments to adjacent carbons:
						List<IAtom> ca2 = m.getConnectedAtomsList(cak);

						for (IAtom ca2l : ca2) {
							if (!r.contains(ca2l)) {
								String f = Fragment[m.indexOf(ca2l)];

								if (!(f instanceof String))
									continue;
								if (f.equals("SsNH2")) {
									HaveACNH2 = true;
								} else if (f.equals("SsOH")) {
									HaveACOH = true;
								}
								if (HaveCOOH(ca2l))
									HaveACCOOH = true;
								if (HaveCO(ca2l))
									HaveACCO = true;
							}
						}

					}

				}

				if (HaveACNH2 && (HaveACOH || HaveACCOOH)) {
					this.HB = 1;
					return;
				}

				if (HaveACOH && HaveACCO) {// covers both CO and COOH
					this.HB = 1;
					return;
				}

//				System.out.println("HaveACNH2="+HaveACNH2);
//				System.out.println("HaveACCOOH="+HaveACCOOH);
//				System.out.println("HaveACOH="+HaveACOH);
//				System.out.println("");

			} // end over loop over atoms in ring

		} // end rs loop
	}

	private boolean HaveCOOH(IAtom a) {

		if (!Fragment[m.indexOf(a)].equals("SdssC")) {
			return false;
		}

		boolean HaveCsOH = false;
		boolean HaveCdO = false;

		List<IAtom> ca = m.getConnectedAtomsList(a);

		for (IAtom cai : ca) {
			if (cai.getFlag(CDKConstants.ISAROMATIC))
				continue;

			if (Fragment[m.indexOf(cai)].equals("SsOH")) {
				HaveCsOH = true;
			} else if (Fragment[m.indexOf(cai)].equals("SdO")) {
				HaveCdO = true;
			}

		}

		if (HaveCsOH && HaveCdO) {
			return true;
		} else {
			return false;
		}

	}

	private boolean HaveCO(IAtom a) {

//		 note: if have COOH will return true in this check

		if (!Fragment[m.indexOf(a)].equals("SdssC")) {
			return false;
		}

		boolean HaveCdO = false;

		List<IAtom> ca = m.getConnectedAtomsList(a);

		for (IAtom cai:ca) {
			if (cai.getFlag(CDKConstants.ISAROMATIC))
				continue;

			if (Fragment[m.indexOf(cai)].equals("SdO")) {
				HaveCdO = true;
			}

		}

		if (HaveCdO) {
			return true;
		} else {
			return false;
		}

	}

	public void writefrags(FileWriter fw) {
		try {

			fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

			fw.write("<caption>ALOGP fragments</caption>\n");

			fw.write("<tr bgcolor=\"#D3D3D3\">\n");
			fw.write("\t<th>Fragment</th>\n");
			fw.write("\t<th>Count</th>\n");
			fw.write("</tr>\n");

			fw.write("<tr><td>CX</td><td>" + CX + "</td></tr>");
			fw.write("<tr><td>NO</td><td>" + NO + "</td></tr>");
			fw.write("<tr><td>PRX</td><td>" + PRX + "</td></tr>");
			fw.write("<tr><td>UB</td><td>" + UB + "</td></tr>");
			fw.write("<tr><td>HB</td><td>" + HB + "</td></tr>");
			fw.write("<tr><td>POL</td><td>" + POL + "</td></tr>");
			fw.write("<tr><td>AMP</td><td>" + AMP + "</td></tr>");
			fw.write("<tr><td>ALK</td><td>" + ALK + "</td></tr>");
			fw.write("<tr><td>RNG</td><td>" + RNG + "</td></tr>");
			fw.write("<tr><td>QN</td><td>" + QN + "</td></tr>");
			fw.write("<tr><td>NO2</td><td>" + NO2 + "</td></tr>");
			fw.write("<tr><td>NCS</td><td>" + NCS + "</td></tr>");
			fw.write("<tr><td>BLM</td><td>" + BLM + "</td></tr>");

			fw.write("</table>\n");

		} catch (Exception e) {

		}
	}
}

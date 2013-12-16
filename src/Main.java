import java.io.IOException;


public class Main {

	public static void main(String[] args) throws IOException {
		BayesienNaif a = new BayesienNaif();
		a.lireCorpusDeTweets();
		a.traiterInfo();
		a.traiterCorpusEval();
		a.afficherConfusion();
		
	}

}

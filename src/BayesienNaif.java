import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;



public class BayesienNaif {
	
	public HashMap<String, Integer> corpusDeMots;
	public LinkedHashMap<ArrayList<Integer>, String> tweetEntrainement;
	public LinkedHashMap<ArrayList<Integer>, String> tweetEvaluation;
	
	public ArrayList<LinkedHashMap<ArrayList<Integer>, String>> aa = new ArrayList<LinkedHashMap<ArrayList<Integer>, String>>();
	
	//Pour savoir le tweet
	public ArrayList<String> azd = new ArrayList<String>();
	
	//Le tableau des betas ?
	LinkedHashMap<double[], Integer> arrayMots = new LinkedHashMap<double[], Integer>();
	
	public int id;
	
	//Nombre de tweet d'entrainement pour chaque tag
	public int nbPositif = 0;
	public int nbNegatif = 0;
	public int nbIrrelevant = 0;
	public int nbNeutre = 0;
	
	//Beta 
	public double[] tabPositif;
	public double[] tabNegatif;
	public double[] tabNeutre;
	public double[] tabIrrelevant;
	
	//Précalcul du alpha
	public double[] tabAlpha;
	
	//k
	public double k = 0.3;
	
	public LinkedHashMap<String, Integer> tabConfusionPositif = new LinkedHashMap<String, Integer>();	
	public LinkedHashMap<String, Integer> tabConfusionNegatif = new LinkedHashMap<String, Integer>();
	public LinkedHashMap<String, Integer> tabConfusionNeutre = new LinkedHashMap<String, Integer>();
	public LinkedHashMap<String, Integer> tabConfusionIrrelevant = new LinkedHashMap<String, Integer>();
	
	public String pathToCorpus = "/net/k13/u/etudiant/sbazin10/Downloads/twitter/train.txt";
	
	
	
	public BayesienNaif(){
		corpusDeMots = new HashMap<String, Integer>();
		tweetEntrainement = new LinkedHashMap<ArrayList<Integer>, String>();
		tweetEvaluation = new LinkedHashMap<ArrayList<Integer>, String>();
		id = 0;
		
		tabConfusionPositif.put("positive", 0);
		tabConfusionPositif.put("negative", 0);		
		tabConfusionPositif.put("irrelevant", 0);
		tabConfusionPositif.put("neutral", 0);
		
		tabConfusionNegatif.put("positive", 0);
		tabConfusionNegatif.put("negative", 0);		
		tabConfusionNegatif.put("irrelevant", 0);
		tabConfusionNegatif.put("neutral", 0);
		
		tabConfusionNeutre.put("positive", 0);
		tabConfusionNeutre.put("negative", 0);		
		tabConfusionNeutre.put("irrelevant", 0);
		tabConfusionNeutre.put("neutral", 0);
		
		tabConfusionIrrelevant.put("positive", 0);
		tabConfusionIrrelevant.put("negative", 0);		
		tabConfusionIrrelevant.put("irrelevant", 0);
		tabConfusionIrrelevant.put("neutral", 0);
	}
	
	
	public void lireCorpusDeTweets() throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(new File(pathToCorpus)));
		String ligne = "";
		
		while ((ligne = reader.readLine()) != null) {
			String[] explodedLine = ligne.split(" ");
			String tag = explodedLine[0].split(",")[0];
			tag = tag.substring(1,tag.length());
			

			ArrayList<Integer> motsDuTweet = new ArrayList<Integer>();
			for(int i=1;i<explodedLine.length;i++){
				if(!corpusDeMots.containsKey(explodedLine[i])){
					corpusDeMots.put(explodedLine[i],id);
					motsDuTweet.add(id);
					id++;
				}
				else{
					motsDuTweet.add(corpusDeMots.get(explodedLine[i]));					
				}							
			}
						
			int a = (int) (Math.random()*5);
			if(a==0){		
				azd.add(ligne);
				tweetEvaluation.put(motsDuTweet,tag);
			}
			else{				
				tweetEntrainement.put(motsDuTweet,tag);
			}
			
			
		}
		
		System.out.println("TweetEvaluation : "+tweetEvaluation.size()+" tweets");
		System.out.println("TweetEntrainement : "+tweetEntrainement.size()+" tweets");			
	}
	
	
	
	public void traiterInfo(){
		
		int size = corpusDeMots.size();
		tabPositif = new double[size];
		tabNegatif = new double[size];
		tabIrrelevant = new double[size];
		tabNeutre = new double[size];
		
		tabAlpha = new double[4];
		int i=0;
		
		for (final Entry<ArrayList<Integer>, String> entry : tweetEntrainement.entrySet()) {
			double tab[] = new double[1];
			//Calcul du nombre de tweet de chaque classe, et on affecte a tab le tableau beta correspondant pour ajouter les mots
			if(entry.getValue().contains("positive")){
				nbPositif++;
				tab = tabPositif;
			}
			else if(entry.getValue().contains("negative")){
				nbNegatif++;
				tab = tabNegatif;
			}
			else if(entry.getValue().contains("irrelevant")){
				nbIrrelevant++;
				tab = tabIrrelevant;
			}
			else if(entry.getValue().contains("neutral")){
				nbNeutre++;
				tab = tabNeutre;
			}
			
			//Remplissage du tableau beta
			for(int s : entry.getKey()){
				tab[s]++;				
			}		
		}
		
		LinkedHashMap<double[], Integer> array = new LinkedHashMap<double[], Integer>();
		array.put(tabPositif,nbPositif);
		array.put(tabNegatif, nbNegatif);
		array.put(tabIrrelevant, nbIrrelevant);
		array.put(tabNeutre, nbNeutre);
		
		for (final Entry<double[], Integer> entry : array.entrySet()) {
			for(int j=0;j<entry.getKey().length;j++){	
				double d = entry.getKey()[j];
				if(d<k){
					d=k;
				}
				else if(d>(entry.getValue()-k)){
					d=entry.getValue()-k;
				}
				d = d/entry.getValue();
				entry.getKey()[j] = d;
				tabAlpha[i] = tabAlpha[i]+Math.log(1-d);				
			}
			i++;
		}
		arrayMots = array;
		
		System.out.println("Positif : "+nbPositif);
		System.out.println("Negatif : "+nbNegatif);
		System.out.println("Irrelevant : "+nbIrrelevant);
		System.out.println("Neutre : "+nbNeutre);
		
	}
	
	
	public void traiterCorpusEval(){
		int i=0;
		int correct=0;
		LinkedHashMap<String, Integer> tab = new LinkedHashMap<String, Integer>();
		
		for (final Entry<ArrayList<Integer>, String> entry : tweetEvaluation.entrySet()) {		

			switch(entry.getValue()){
				case "positive" : tab = tabConfusionPositif;
				break;
				case "negative" : tab = tabConfusionNegatif;
				break;
				case "irrelevant" : tab = tabConfusionIrrelevant;
				break;
				case "neutral" : tab = tabConfusionNeutre;
				break;							
			}
						
			int type = traiterTweet(entry.getKey(), entry.getValue());	
			if(type == 0){
				int a = tab.get("positive");
				a++;
				tab.put("positive",a);
				//System.out.println(entry.getValue()+"  :  Positif");
				if(entry.getValue().contains("positive")){
					correct++;
				}
			}
			else if(type == 1){
				int a = tab.get("negative");
				a++;
				tab.put("negative",a);
				//System.out.println(entry.getValue()+"  :  Negatif");
				if(entry.getValue().contains("negative")){
					correct++;
				}
			}
			else if(type == 2){
				int a = tab.get("irrelevant");
				a++;
				tab.put("irrelevant",a);
				//System.out.println(entry.getValue()+"  :  Irrelevant");
				if(entry.getValue().contains("irrelevant")){
					correct++;
				}
			}
			else if(type == 3){
				int a = tab.get("neutral");
				a++;
				tab.put("neutral",a);
				//System.out.println(entry.getValue()+"  :  Neutre");
				if(entry.getValue().contains("neutral")){
					correct++;
				}
			}
			i++;
		}
		
		System.out.println("Total de tweet : "+i);
		System.out.println("Bonnes réponses : "+(double)correct/(double)i);			
	}
	
	
	public int traiterTweet(ArrayList<Integer> tweet, String tweetComplet){
		double result = Integer.MIN_VALUE;
		int type = Integer.MIN_VALUE;
		int i = 0;
		
		for (final Entry<double[], Integer> entry : arrayMots.entrySet()) {
			double resultInter = tabAlpha[i];			
			for(int mot : tweet){		
				resultInter = resultInter + Math.log(entry.getKey()[mot]);
				resultInter = resultInter - Math.log(1-entry.getKey()[mot]);
			}
			double salut = entry.getValue()/(double)tweetEntrainement.size();
			resultInter = resultInter+Math.log(salut);
			if(resultInter> result){
				result = resultInter;
				type = i;
			}
			i++;
		}
		
		return type;		
	}
	
	public void afficherConfusion(){
		
		System.out.println("         Positif   Negatif   Irrelevant  Neutre");
		System.out.print("Positif      ");
		for (final Entry<String, Integer> entry : tabConfusionPositif.entrySet()) {
			System.out.print(entry.getValue()+"        ");
		}
		System.out.println();
		System.out.print("Negatif      ");
		for (final Entry<String, Integer> entry : tabConfusionNegatif.entrySet()) {
			System.out.print(entry.getValue()+"        ");
		}		
		System.out.println();
		System.out.print("Irrelevant   ");
		for (final Entry<String, Integer> entry : tabConfusionIrrelevant.entrySet()) {
			System.out.print(entry.getValue()+"        ");
		}
		System.out.println();
		System.out.print("Neutre       ");
		for (final Entry<String, Integer> entry : tabConfusionNeutre.entrySet()) {
			System.out.print(entry.getValue()+"       ");
		}
		
	
	}
}

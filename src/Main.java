import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Main {
    public static int globalIndex=0;
    public static String extractPageTitle(String html) {
        try {
            Document doc = Jsoup.parse(html);
            return doc.select("title").first().text();
        } catch (Exception e) {
            return "Error: no title tag found in page source!";
        }
    }

    public static void retrieveRssContent(String rssUrl) {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(
                    xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");

            for (int i = 0; i < 5; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent())
                    ;
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).
                            getTextContent());
                }
            }
        } catch (Exception e) {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }

    public static String extractRssUrl(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }

    public static String fetchPageSource(String urlString) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
    }

    private static String toString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);

        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        int userChoice ;
        int checkForRepetitiousUrl=0;
        int forChooseShowUpdates=0;
        String line;
        String forAddUrl;
        String htmlOfRss = null;
        String removeUrl;
        File dataFile=new File("data.txt");
        FileReader readDataFile;
        BufferedReader readBufferedDataFile;
        FileWriter writeFileData;
        BufferedWriter bufferedWriterFileData;
        Website[] website = new Website[100];
        try {
            readDataFile = new FileReader(dataFile);
            readBufferedDataFile = new BufferedReader(readDataFile);
            while ((line=readBufferedDataFile.readLine())!=null){
                String [] urlInformation = line.split(";");
                website[globalIndex] = new Website();
                website[globalIndex].setTitle(urlInformation[0]);
                website[globalIndex].setUrl(urlInformation[1]);
                website[globalIndex].setRss(urlInformation[2]);
                globalIndex++;
            }
        }
        catch (IOException e){
            System.out.println("can't read file");
        }
        Scanner scannerForShowUpdates = new Scanner(System.in);
        Scanner scannerForReadUserChoice = new Scanner(System.in);
        Scanner scannerForReadUrl= new Scanner(System.in);
        Scanner scannerForRemoveUrl = new Scanner(System.in);
        System.out.println("Welcome to Rss reader ");
        while(true){
            System.out.println("[1] Show updates");
            System.out.println("[2] Add URL");
            System.out.println("[3] Remove URl");
            System.out.println("[4] Exit");
            try {
                userChoice = scannerForReadUserChoice.nextInt();
                if (userChoice == 1) {
                    System.out.println("Show updates for :");
                    System.out.println("[0] All websites");
                    for (int i = 0; i < globalIndex; i++) {
                        if (website[i] != null) {
                            System.out.println("[" + (i + 1) + "] " + website[i].getTitle());
                        }
                    }
                    try {
                        forChooseShowUpdates = scannerForShowUpdates.nextInt();
                        if (forChooseShowUpdates == 0) {
                            for (int i = 0; i < globalIndex; i++) {
                                retrieveRssContent(website[i].getRss());
                            }
                        } else {
                            retrieveRssContent(website[forChooseShowUpdates - 1].getRss());
                        }
                    }
                    catch (Exception exception){
                        System.out.println("Wrong Value");
                        scannerForShowUpdates.nextLine();
                    }

                }
                if (userChoice == 2) {
                    System.out.println("please enter website URL to add : ");
                    forAddUrl = scannerForReadUrl.nextLine();
                    for (int i = 0; i < globalIndex; i++) {
                        if (forAddUrl.compareTo(website[i].getUrl()) == 0) {
                            System.out.println(forAddUrl + " already exists");
                            checkForRepetitiousUrl = 1;
                            break;
                        }
                    }
                    if (checkForRepetitiousUrl == 0) {
                        website[globalIndex] = new Website();
                        website[globalIndex].setUrl(forAddUrl);
                        try {
                            website[globalIndex].setRss(extractRssUrl(forAddUrl));
                        } catch (IOException e) {
                            System.out.println("Can't find Rss ");
                            continue;
                        }
                        try {
                            htmlOfRss = fetchPageSource(forAddUrl);
                        } catch (Exception e) {
                            System.out.println("Can't find Rss");
                            continue;
                        }
                        try {
                            website[globalIndex].setTitle(extractPageTitle(htmlOfRss));
                        } catch (Exception e) {
                            System.out.println("Can't find title");
                            continue;
                        }
                        System.out.println("Added " + forAddUrl + " successfully");
                        globalIndex++;
                    }

                }
                if (userChoice == 3) {
                    System.out.println("Please enter website URL to remove :");
                    int checkForExistUrlForRemove = 0;
                    removeUrl = scannerForRemoveUrl.nextLine();
                    for (int i = 0; i < globalIndex; i++) {
                        if (removeUrl.equals(website[i].getUrl())) {
                            checkForExistUrlForRemove = 1;
                            for (int j = i + 1; j < globalIndex; j++) {
                                website[j - 1] = website[j];
                            }
                            website[globalIndex - 1] = null;
                            globalIndex--;
                            System.out.println("Deleted successfully");
                            break;
                        }
                    }
                    if (checkForExistUrlForRemove == 0) {
                        System.out.println("couldn't find " + removeUrl);
                    }
                }
                if (userChoice == 4) {
                    try {
                        writeFileData = new FileWriter(dataFile);
                        bufferedWriterFileData = new BufferedWriter(writeFileData);
                        for (int i = 0; i < globalIndex; i++) {
                            bufferedWriterFileData.write(website[i].getTitle() + ";" + website[i].getUrl() + ";" + website[i].getRss() + "\n");
                        }
                        bufferedWriterFileData.close();
                        break;
                    } catch (IOException ex) {
                        System.out.println("Can't write");
                        continue;
                    }

                }
                if (userChoice<0||userChoice>4){
                    System.out.println("out of range");
                }
            }
            catch (Exception e){
                System.out.println("Wrong value");
                scannerForReadUserChoice.nextLine();
                continue;
            }
        }

    }
}
package isistan.ayrinfo;

/**
 * Indexador de archivos utilizando Lucene
 * @author Marcelo Armentano
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Indexa todos los archivos de texto de un directorio.
 */
public class IndexFiles {

    MovieSearch app;

    public IndexFiles(MovieSearch app) {
        this.app = app;
    }

    public void index(String docsPath, String indexPath, boolean create) {

        File docDir;
        Date start = new Date();
        try {

            docDir = new File(docsPath);
            if (!docDir.exists() || !docDir.canRead()) {
                app.indexLogAppend("La carpeta de documentos '"
                        + docDir.getAbsolutePath()
                        + "' no existe o no se puede leer");
                return;
            }

            Directory dir = FSDirectory.open(new File(indexPath));
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40,
                    analyzer);

            if (create) {
                // Crea un nuevo índice en la carpeta, eliminando los documentos
                // previamente indexados
                iwc.setOpenMode(OpenMode.CREATE);
            } else {
                // Agrega documentos a un índice existente
                iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir);

            writer.close();

            Date end = new Date();
            app.indexLogAppend(end.getTime() - start.getTime()
                    + " milisegundos totales");

        } catch (IOException e) {
            app.indexLogAppend(" caught a " + e.getClass()
                    + "\n with message: " + e.getMessage());
        }
    }
    
    public CharSequence fromFile(FileInputStream input) throws IOException {
        FileChannel channel = input.getChannel();
    
        // Create a read-only CharBuffer on the file
        ByteBuffer bbuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int)channel.size());
        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
        return cbuf;
    }

    /**
     * Indexa el archivo <i>file</i> dado utilizando el IndexWritter dado. Si
     * <i>file</i> es un directorio, la indexación es recursiva a todos sus
     * archivos y subdirectorios
     * 
     * @param writer
     *            índice donde almacenar el documento/directorio dado
     * @param file
     *            El archivo a indexar o el directorio conteniendo los archivos
     *            a indexar
     * @throws IOException
     *             Si ocurre un error de Entrada/Salida
     */
    private void indexDocs(IndexWriter writer, File file) throws IOException {

        // Evita indexar archivos que no se pueden leer
        if (file.canRead()) {
            // Si es un directorio, llamado recursivo
            if (file.isDirectory()) {
                String[] files = file.list();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        indexDocs(writer, new File(file, files[i]));
                    }
                }
            } else {
                // Es un archivo
                FileInputStream fis;
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException fnfe) {
                    return;
                }

                try {
                	CharSequence movieContent = fromFile(fis);
                	
                	Pattern actorsPattern = Pattern.compile("<[^>\"]+?\"castactor\"[^>]+?>([^<]+)</td>");
                	Pattern directorPattern = Pattern.compile(">Director</td>[\n\r]*.+?fieldvalue\">([^<]+)</td>");
                	Pattern genrePattern = Pattern.compile("</div>[\n\r ]*<span class=\"fieldvaluelarge\">([^<]+)</span>");
                	
                	Matcher actorsMatcher = actorsPattern.matcher(movieContent);
                	Matcher directorMatcher = directorPattern.matcher(movieContent);
                	Matcher genreMatcher = genrePattern.matcher(movieContent);
                	
                	String actors = "";
                	String director = "";
                	String genres = "";
                	
                	//Extraigo actores
                	while (actorsMatcher.find()) {
                		if(actors.isEmpty()){
                			actors = actorsMatcher.group(1);
                		}
                		else{
                			actors = actors + ", " + actorsMatcher.group(1);
                		}
            		}
                	
                	//Extraigo el director
                	while (directorMatcher.find()) {
                		if(director.isEmpty()){
                			director = directorMatcher.group(1);
                		}
            		}
                	
                	//Extraigo la lista de generos
                	while (genreMatcher.find()) {
                		if(genres.isEmpty()){
                			genres = genreMatcher.group(1);
                		}
            		}                	

                    // crear un nuevo documento vacío
                    Document doc = new Document();

                    // Agrega el path del archivo como campo "path".
                    // Será posible buscar por este campo (indexado), pero no
                    // analizado
                    Field pathField = new StringField("path", file.getPath(),
                            Field.Store.YES);
                    doc.add(pathField);

                    // Agrega la fecha de última modificación como campo
                    // "modified"
                    //doc.add(new StringField("modified",
                    //        DateTools.timeToString(file.lastModified(), DateTools.Resolution.MINUTE),
                    //        Field.Store.YES));

                    // Agregar el contenido del archivo como campo "contents"
                    // Especificamos un Reader para toquenizar e indexar el
                    // texto, pero no almacenarlo
                    
                    ContentHandler textHandler = new BodyContentHandler();
                    Metadata metadata = new Metadata();
                    ParseContext parseContext = new ParseContext();
                    HtmlParser parser = new HtmlParser();
                    
                    try {
						parser.parse(fis, textHandler, metadata, parseContext);
						InputStream is = new ByteArrayInputStream(textHandler.toString().getBytes());
						TextField txt = new TextField("contents", new BufferedReader(new InputStreamReader(is)));
						
						if(textHandler.toString().contains("Robert De Niro")){
							txt.setBoost(new Float(1.5));
						}
						
						doc.add(new TextField("actors", new BufferedReader(new InputStreamReader(new ByteArrayInputStream(actors.getBytes())))));
						doc.add(new TextField("director", new BufferedReader(new InputStreamReader(new ByteArrayInputStream(director.getBytes())))));
						doc.add(new TextField("genres", new BufferedReader(new InputStreamReader(new ByteArrayInputStream(genres.getBytes())))));
						doc.add(txt);
						
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TikaException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

                    if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                        // se trata de un nuevo índice, por lo que simplemente
                        // agregamos el documento:
                        app.indexLogAppend("Indexando " + file + " ...");
                        writer.addDocument(doc);
                    } else {
                        // Se trata de un índice existente, por lo que puede
                        // existir una copia vieja de este documento ya indexada
                        // Es por esto que utilizamos updateDocument para
                        // reemplazar el documento viejo que coincida con el
                        // path exacto, si existe
                        app.indexLogAppend("Actualizando " + file + " ...");
                        writer.updateDocument(new Term("path", file.getPath()),
                                doc);
                    }

                } finally {
                    fis.close();
                }
            }
        }
    }
}

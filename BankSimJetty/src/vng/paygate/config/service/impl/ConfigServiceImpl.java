package vng.paygate.config.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import vng.paygate.config.service.IConfigService;
import vng.paygate.domain.bo.BoBaseResponse;
import vng.paygate.domain.bo.BoModuleConfig;

/**
 *
 * @author VuongTM
 */
public class ConfigServiceImpl<T extends BoModuleConfig> implements IConfigService<T>  {

    private String xmlConfigFile;
    private String propsConfigFile;
    private String envConfigFile;
    private Class<T> genericType;
    private T moduleConfig;
    
    public ConfigServiceImpl(String envConfigFile, String propsConfigFile, String xmlConfigFile, Class<T> genericType) 
            throws IOException, FileNotFoundException, ConfigurationException, JAXBException{
        this.genericType = genericType;
        this.envConfigFile = envConfigFile;
        this.propsConfigFile = propsConfigFile;
        this.xmlConfigFile = xmlConfigFile;
        this.moduleConfig = loadModuleConfig();
    }

    @Override
    public T getModuleConfig() {
        return this.moduleConfig;
    }

    @Override
    public void refreshModuleConfig() {
        try {
            this.moduleConfig = loadModuleConfig();
        } catch (IOException ex) {
            Logger.getLogger(ConfigServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConfigurationException ex) {
            Logger.getLogger(ConfigServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(ConfigServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private T loadModuleConfig() throws IOException, FileNotFoundException, ConfigurationException, JAXBException{
        System.out.println("Start load config!!!!!!!!");
        String env = getEnvironment();
        System.out.println("config env: " + env);
        String xmlFile = "/config/" + env + this.xmlConfigFile;

        T configs = loadXmlConfigs(xmlFile);

        String propsFile = "/config/" + env + this.propsConfigFile;

        loadProppertiesConfigs(configs, propsFile);
        configs.setEnv(env);

        return configs;
    }
    
    private String getEnvironment() throws FileNotFoundException, IOException, ConfigurationException{
        File file = loadPropetiesFile(this.envConfigFile);
        InputStream is = new FileInputStream(file);
        if (is == null) {
            throw new IOException("env.properties not found in classpath");
        }
        Properties props = new Properties();
        props.load(is);
        String env = props.getProperty("env").trim();
        return env;
    }
    
    private File loadPropetiesFile(String propertiesFile) throws ConfigurationException{
        URL url = ConfigServiceImpl.class.getResource(propertiesFile);
        FileConfiguration fileConfig = new PropertiesConfiguration(url);
        fileConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
        return fileConfig.getFile();
    }
    
    private T loadXmlConfigs(String xmlFile) throws JAXBException, ConfigurationException, FileNotFoundException, IOException{
        JAXBContext jaxbContext = JAXBContext.newInstance(new Class[]{this.genericType});
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        File file = loadXmlFile(xmlFile);
        Reader reader = new FileReader(file);
        T configs = (T)(BoModuleConfig) unmarshaller.unmarshal(reader);
        reader.close();
        return configs;
    }
    
    private File loadXmlFile(String xmlFile)
            throws ConfigurationException {
        URL url = ConfigServiceImpl.class.getResource(xmlFile);
        FileConfiguration fileConfig = new XMLConfiguration(url);
        fileConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
        return fileConfig.getFile();
    }
    
    private void loadProppertiesConfigs(T moduleConfig, String propsConfigFile)
            throws IOException, ConfigurationException {
        File file = loadPropetiesFile(propsConfigFile);
        InputStream is = new FileInputStream(file);
        if (is == null) {
            throw new IOException(propsConfigFile + " not found in classpath");
        }
        Properties props = new Properties();
        props.load(is);
        is.close();
        moduleConfig.setProperties(props);
    }
}

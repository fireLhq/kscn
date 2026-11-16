package top.kscn.service.impl;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import top.kscn.service.IpLocationService;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Map;

@Service
public class IpLocationServiceImpl implements IpLocationService {
    private static final Logger log = LoggerFactory.getLogger(IpLocationServiceImpl.class);

    @Value("${geoip.database-path:classpath:geoip/GeoLite2-City.mmdb}")
    private String databasePath;

    private DatabaseReader databaseReader;

    @PostConstruct
    public void initialize() {
        Resource resource = databasePath.startsWith("classpath:") || databasePath.startsWith("file:")
                ? new DefaultResourceLoader().getResource(databasePath)
                : new FileSystemResource(databasePath);
        if (!resource.exists()) {
            log.warn("GeoIP database not found: {}. IP locations will use the original IP.", databasePath);
            return;
        }
        // Classpath resources inside an executable JAR cannot be converted to ordinary files.
        try (InputStream input = resource.getInputStream()) {
            databaseReader = new DatabaseReader.Builder(input).build();
            log.info("Loaded GeoIP database: {}", databasePath);
        } catch (IOException e) {
            log.warn("Unable to load GeoIP database: {}", databasePath, e);
        }
    }

    @Override
    public String resolve(String ip) {
        if (databaseReader == null || ip == null || ip.isBlank()) {
            return null;
        }
        try {
            CityResponse city = databaseReader.city(InetAddress.getByName(ip));
            String country = localizedName(city.getCountry().getNames(), city.getCountry().getName(), city.getCountry().getIsoCode());
            String region = localizedName(city.getMostSpecificSubdivision().getNames(), city.getMostSpecificSubdivision().getName(), null);
            String cityName = localizedName(city.getCity().getNames(), city.getCity().getName(), null);
            if (country == null && region == null && cityName == null) {
                return null;
            }
            if (country != null && region != null && cityName != null) {
                return country + "-" + region + "-" + cityName;
            }
            if (cityName != null) {
                return country == null ? cityName : country + "-" + cityName;
            }
            return region != null ? (country == null ? region : country + "-" + region) : country;
        } catch (Exception e) {
            log.debug("Unable to resolve IP location: {}", ip, e);
            return null;
        }
    }

    private String localizedName(Map<String, String> names, String fallback, String code) {
        if (names != null) {
            String chineseName = names.get("zh-CN");
            if (chineseName != null && !chineseName.isBlank()) {
                return chineseName;
            }
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return code;
    }

    @PreDestroy
    public void close() throws IOException {
        if (databaseReader != null) {
            databaseReader.close();
        }
    }
}

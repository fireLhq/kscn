package top.kscn.service.impl;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.kscn.service.IpLocationService;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class IpLocationServiceImpl implements IpLocationService {
    private static final Logger log = LoggerFactory.getLogger(IpLocationServiceImpl.class);

    @Value("${geoip.database-path:data/geoip/GeoLite2-City.mmdb}")
    private String databasePath;

    private DatabaseReader databaseReader;

    @PostConstruct
    public void initialize() {
        Path path = Path.of(databasePath).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            log.warn("GeoIP database not found: {}. IP locations will use the original IP.", path);
            return;
        }
        try {
            databaseReader = new DatabaseReader.Builder(path.toFile()).build();
            log.info("Loaded GeoIP database: {}", path);
        } catch (IOException e) {
            log.warn("Unable to load GeoIP database: {}", path, e);
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
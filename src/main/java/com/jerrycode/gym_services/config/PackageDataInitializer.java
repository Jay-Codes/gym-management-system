package com.jerrycode.gym_services.config;

import com.jerrycode.gym_services.data.dao.PackageRepository;
import com.jerrycode.gym_services.data.vo.Packages;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class PackageDataInitializer {

    private final PackageRepository packageRepository;

    public PackageDataInitializer(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    @PostConstruct
    public void init() {
        if (packageRepository.count() == 0) {
            Packages oneDay = Packages.builder()
                    .name("One Day")
                    .priceUSD(8)
                    .priceTZS(20000)
                    .duration(1)
                    .build();

            Packages twoWeeks = Packages.builder()
                    .name("Two Weeks")
                    .priceUSD(56)
                    .priceTZS(150000)
                    .duration(14)
                    .build();

            Packages oneMonth = Packages.builder()
                    .name("One Month")
                    .priceUSD(112)
                    .priceTZS(300000)
                    .duration(30)
                    .build();

            // Add more packages as needed...

            packageRepository.save(oneDay);
            packageRepository.save(twoWeeks);
            packageRepository.save(oneMonth);

            System.out.println("Default packages initialized successfully");
        }
    }
}

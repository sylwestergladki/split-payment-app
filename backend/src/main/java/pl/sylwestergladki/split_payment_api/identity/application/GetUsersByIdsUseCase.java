package pl.sylwestergladki.split_payment_api.identity.application;

import org.springframework.stereotype.Service;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUser;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUserRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

@Service
public class GetUsersByIdsUseCase {

    private final AppUserRepository appUserRepository;

    public GetUsersByIdsUseCase(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public List<AppUser> execute(List<UserId> userIds) {
        return appUserRepository.findAllByIds(userIds);
    }
}
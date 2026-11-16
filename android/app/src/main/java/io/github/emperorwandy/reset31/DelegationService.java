package io.github.emperorwandy.reset31;


import com.google.androidbrowserhelper.locationdelegation.LocationDelegationExtraCommandHandler;


public class DelegationService extends
        com.google.androidbrowserhelper.trusted.DelegationService {
    @Override
    public void onCreate() {
        super.onCreate();

        
            // Answer Chrome's location permission question first (see LocationPermissionHandler),
            // then let the library provide the location updates.
            registerExtraCommandHandler(new LocationPermissionHandler());
            registerExtraCommandHandler(new LocationDelegationExtraCommandHandler());
        
    }
}


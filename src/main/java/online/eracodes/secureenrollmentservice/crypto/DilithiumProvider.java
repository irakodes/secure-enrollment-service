package online.eracodes.secureenrollmentservice.crypto;


import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;

public class DilithiumProvider extends Provider {

    @SuppressWarnings("removal")
    public DilithiumProvider() {
        super("Dilithium Provider", "0.1", "Secure Enrollment Service - For Experimental Post-Quantum Algorithms Use");

        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            /*
             * Key(pair) Generator engines
             */
            put("KeyPairGenerator.Dilithium",
                    "net.thiim.dilithium.provider.DilithiumKeyPairGenerator");
            put("Alg.Alias.KeyPairGenerator.Dilithium", "Dilithium");

            /*
             * Key factories
             */
            put("KeyFactory.Dilithium",
                    "net.thiim.dilithium.provider.DilithiumKeyFactory");
            put("Alg.Alias.KeyFactory.Dilithium", "Dilithium");

            /*
             * Key factories
             */
            put("Signature.Dilithium",
                    "net.thiim.dilithium.provider.DilithiumSignature");
            put("Alg.Alias.Signature.Dilithium", "Dilithium");

            return null;
        });
    }

    private static final long serialVersionUID = 1L;
}

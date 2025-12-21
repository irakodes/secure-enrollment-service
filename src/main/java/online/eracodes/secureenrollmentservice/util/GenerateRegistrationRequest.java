package online.eracodes.secureenrollmentservice.util;

import online.eracodes.protobuf.enrollment.EnrollmentProto;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Helper utility to generate a protobuf registration request file for testing.
 * 
 * Usage:
 *   java GenerateRegistrationRequest <email> <registration_code> [output_file]
 * 
 * Example:
 *   java GenerateRegistrationRequest test@example.com abc123def4567890abcd request.bin
 */
public class GenerateRegistrationRequest {
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java GenerateRegistrationRequest <email> <registration_code> [output_file]");
            System.err.println("");
            System.err.println("Example:");
            System.err.println("  java GenerateRegistrationRequest test@example.com abc123def4567890abcd request.bin");
            System.err.println("");
            System.err.println("Note: registration_code must be a 20-character hex string with checksum");
            System.err.println("      You can generate one using: StringsUtil.getRegistrationCode(email)");
            System.exit(1);
        }
        
        String email = args[0];
        String registrationCode = args[1];
        String outputFile = args.length > 2 ? args[2] : "request.bin";
        
        // Validate registration code length
        if (registrationCode.length() != 20) {
            System.err.println("Error: Registration code must be exactly 20 characters");
            System.err.println("       Got: " + registrationCode.length() + " characters");
            System.exit(1);
        }
        
        // Validate checksum
        if (!StringsUtil.isRegistrationCodeVerified(registrationCode)) {
            System.err.println("Warning: Registration code checksum validation failed!");
            System.err.println("         The code may still work if the database has the matching hash.");
        }
        
        // Create the request
        EnrollmentProto.RegisterUserRequest request = EnrollmentProto.RegisterUserRequest.newBuilder()
                .setEmail(email)
                .setRegistrationCode(registrationCode)
                .build();
        
        // Write to file
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            request.writeTo(fos);
            System.out.println("✓ Successfully created protobuf request file: " + outputFile);
            System.out.println("  Email: " + email);
            System.out.println("  Registration Code: " + registrationCode);
            System.out.println("");
            System.out.println("You can now use this file with curl or HTTPie:");
            System.out.println("  curl -k -X POST https://localhost:8443/api/register/complete \\");
            System.out.println("    -H 'Content-Type: application/octet-stream' \\");
            System.out.println("    -H 'Accept: application/octet-stream' \\");
            System.out.println("    --data-binary @" + outputFile);
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
            System.exit(1);
        }
    }
}

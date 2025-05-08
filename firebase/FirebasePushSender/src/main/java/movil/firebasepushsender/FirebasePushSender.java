/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package movil.firebasepushsender;

import java.util.Scanner;

/**
 *
 * @author devxa
 */
public class FirebasePushSender {

   public static void main(String[] args) {
        try {
            FirebaseInitializer.init();
            Scanner scanner = new Scanner(System.in);
            System.out.print("🔑 Ingresa el token del dispositivo: ");
            String token = scanner.nextLine();

            System.out.print("📣 Título del mensaje: ");
            String title = scanner.nextLine();

            System.out.print("📝 Cuerpo del mensaje: ");
            String body = scanner.nextLine();

            PushSender.send(token, title, body);
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar Firebase o enviar notificación:");
            e.printStackTrace();
        }
    }
}

import java.sql.*;

public class TestDatasetCouples {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/annotations_db";
        String username = "root";
        String password = "";
        
        System.out.println("=== DIAGNOSTIC DES COUPLES DE TEXTE ===");
        
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connexion réussie !");
            
            // 1. Vérifier les datasets
            System.out.println("\n1. Datasets disponibles :");
            Statement stmt1 = connection.createStatement();
            ResultSet rs1 = stmt1.executeQuery("SELECT id, name, description FROM dataset");
            while (rs1.next()) {
                System.out.println("  Dataset ID: " + rs1.getLong("id") + 
                                 ", Name: " + rs1.getString("name") + 
                                 ", Description: " + rs1.getString("description"));
            }
            rs1.close();
            stmt1.close();
            
            // 2. Vérifier les couples de texte
            System.out.println("\n2. Couples de texte :");
            Statement stmt2 = connection.createStatement();
            ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*) as total FROM couple_text");
            if (rs2.next()) {
                System.out.println("  Total couples de texte : " + rs2.getInt("total"));
            }
            rs2.close();
            stmt2.close();
            
            // 3. Vérifier les couples par dataset
            System.out.println("\n3. Couples par dataset :");
            Statement stmt3 = connection.createStatement();
            ResultSet rs3 = stmt3.executeQuery(
                "SELECT d.id, d.name, COUNT(ct.id) as couple_count " +
                "FROM dataset d " +
                "LEFT JOIN couple_text ct ON d.id = ct.dataset_id " +
                "GROUP BY d.id, d.name " +
                "ORDER BY d.id"
            );
            while (rs3.next()) {
                System.out.println("  Dataset ID: " + rs3.getLong("id") + 
                                 ", Name: " + rs3.getString("name") + 
                                 ", Couples: " + rs3.getInt("couple_count"));
            }
            rs3.close();
            stmt3.close();
            
            // 4. Vérifier quelques exemples de couples
            System.out.println("\n4. Exemples de couples (premiers 5) :");
            Statement stmt4 = connection.createStatement();
            ResultSet rs4 = stmt4.executeQuery(
                "SELECT ct.id, ct.dataset_id, " +
                "SUBSTRING(ct.text_1, 1, 50) as text1_preview, " +
                "SUBSTRING(ct.text_2, 1, 50) as text2_preview " +
                "FROM couple_text ct " +
                "LIMIT 5"
            );
            while (rs4.next()) {
                System.out.println("  Couple ID: " + rs4.getLong("id") + 
                                 ", Dataset ID: " + rs4.getLong("dataset_id"));
                System.out.println("    Text 1: " + rs4.getString("text1_preview") + "...");
                System.out.println("    Text 2: " + rs4.getString("text2_preview") + "...");
                System.out.println();
            }
            rs4.close();
            stmt4.close();
            
            // 5. Vérifier la structure de la table
            System.out.println("\n5. Structure de la table couple_text :");
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "couple_text", null);
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String isNullable = columns.getString("IS_NULLABLE");
                System.out.println("  " + columnName + " (" + dataType + 
                                 ", size: " + columnSize + 
                                 ", nullable: " + isNullable + ")");
            }
            columns.close();
            
            connection.close();
            System.out.println("\n✅ Diagnostic terminé !");
            
        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }
}

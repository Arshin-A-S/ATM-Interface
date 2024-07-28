import net.proteanit.sql.DbUtils;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class TransactionHistory extends JFrame{
    private JPanel transactionHistoryPanel;
    private JButton closeButton;
    private JTable TransactionHistoryTable;
    private Connection con;
    private PreparedStatement getRecords;

    TransactionHistory(MainMenu mm, int userId){
        connectDatabase();
        loadTransactionHistoryTable(userId);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                mm.setVisible(true);
            }
        });
        this.setContentPane(this.transactionHistoryPanel);
        this.setTitle("ATM");
        this.setSize(1000,1000);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void connectDatabase(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ATM", "root", "1234");
        } catch(Exception SqlConnE){
            SqlConnE.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Connection Error: "+SqlConnE.getMessage(), "Database Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactionHistoryTable(int userId){
        try{
            String getRecordQuery = "select * from transactionhistory where user_id=?";
            getRecords = con.prepareStatement(getRecordQuery);
            getRecords.setInt(1, userId);
            ResultSet TableResult = getRecords.executeQuery();
            TransactionHistoryTable.setModel(DbUtils.resultSetToTableModel(TableResult));
        } catch(SQLException SqlQueryE){
            SqlQueryE.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Query Error: Couldn't load transaction history!", "Database Query Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

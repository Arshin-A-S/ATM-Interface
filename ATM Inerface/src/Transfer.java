import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transfer extends JFrame{
    private JTextField destAccountField;
    private JTextField amountField;
    private JButton confirmButton;
    private JButton cancelButton;
    private JPanel transferPanel;
    private Connection con;

    Transfer(MainMenu mm, int userId){
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectDatabase();
                try{
                    int senderAccNo;
                    int balance;
                    int transferAmount = Integer.parseInt(amountField.getText().strip());
                    int destAcc = Integer.parseInt(destAccountField.getText().strip());
                    int destUserId;
                    int destBalance;
                    String getDestinationAccQuery = "select balance, user_id from account where account_no=?";
                    String getSourceAccQuery = "select balance, account_no from account where user_id=?";

                    //Get destination bank balance
                    PreparedStatement getDestBalance= con.prepareStatement(getDestinationAccQuery);
                    getDestBalance.setInt(1, destAcc);
                    ResultSet destAccount = getDestBalance.executeQuery();
                    if(destAccount.next()){
                        destUserId = destAccount.getInt("user_id");
                        destBalance  = destAccount.getInt("balance");
                        //Get Source Bank Balance
                        PreparedStatement getSourceAccount= con.prepareStatement(getSourceAccQuery);
                        getSourceAccount.setInt(1, userId);
                        ResultSet sourceAccount = getSourceAccount.executeQuery();
                        if(sourceAccount.next()){
                            balance = sourceAccount.getInt("balance");
                            senderAccNo = sourceAccount.getInt("account_no");
                            //Updating balance
                            if((balance-transferAmount)>0){
                                boolean updated = updateBalance(userId, balance - transferAmount, con);
                                if(updated){
                                    updateTransactionHistory(userId, "Transfer", senderAccNo, destAcc, transferAmount, con);
                                    updateBalance(destUserId, destBalance + transferAmount, con);
                                    JOptionPane.showMessageDialog(Transfer.this, "Transaction Successful!!\n"+transferAmount+" has been withdrawn from your account"  , "Transaction Completed", JOptionPane.INFORMATION_MESSAGE);
                                    dispose();
                                    mm.setVisible(true);
                                } else{
                                    JOptionPane.showMessageDialog(Transfer.this, "Transaction failed! Please try again or contact your bank"  , "Transaction Completed", JOptionPane.ERROR_MESSAGE);
                                    updateBalance(userId, balance + transferAmount, con);
                                    updateBalance(destUserId, destBalance - transferAmount, con);
                                    dispose();
                                    mm.setVisible(true);
                                }
                            } else{
                                JOptionPane.showMessageDialog(Transfer.this, "You do not have enough funds in your account!", "Insufficient Funds", JOptionPane.ERROR_MESSAGE);
                            }
                        } else{
                            JOptionPane.showMessageDialog(Transfer.this, "Failed to access your bank account", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }else{
                        JOptionPane.showMessageDialog(Transfer.this, "Failed to find the bank account. Enter correct account number and try again.", "Unknown Bank Account", JOptionPane.ERROR_MESSAGE);
                    }
                } catch(SQLException SqlQueryE){
                    SqlQueryE.printStackTrace();
                    JOptionPane.showMessageDialog(Transfer.this, "Error: Database Query Error: "+SqlQueryE.getMessage(), "Database Query Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                mm.setVisible(true);
            }
        });
        this.setContentPane(this.transferPanel);
        this.setTitle("ATM");
        this.setSize(1000,1000);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static boolean updateBalance(int userId, int balance, Connection con){
        try{
            //Updating balance
            String updateBalanceQuery = "update account set balance=? where user_id=?";
            PreparedStatement updateBalance = con.prepareStatement(updateBalanceQuery);
            updateBalance.setInt(1, balance);
            updateBalance.setInt(2, userId);
            int balanceUpdated = updateBalance.executeUpdate();
            if(balanceUpdated > 0){
                return true;
            } else{
                return false;
            }
        } catch(SQLException SqlQueryE){
            SqlQueryE.printStackTrace();
            return false;
        }
    }

    private static void updateTransactionHistory(int userId, String type, int sourceAccNo, int destAccNo, int amount, Connection con){
        //Get the date of transaction
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = currentDate.format(formatter);
        //Insert the values
        try{
            String insertQuery = "insert into transactionhistory(user_id,transac_type,transac_date,source_account,destination_account,amount) values(?,?,?,?,?,?)";
            PreparedStatement insertStatement = con.prepareStatement(insertQuery);
            insertStatement.setInt(1,userId);
            insertStatement.setString(2, type);
            insertStatement.setString(3, date);
            insertStatement.setInt(4, sourceAccNo);
            insertStatement.setInt(5, destAccNo);
            insertStatement.setInt(6, amount);
            insertStatement.executeUpdate();

        } catch(SQLException SqlQueryE){
            SqlQueryE.printStackTrace();
        }
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
}

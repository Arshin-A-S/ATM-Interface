import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Withdraw extends JFrame{
    private JTextField textField1;
    private JButton confirmButton;
    private JButton cancelButton;
    private JPanel withdrawPanel;
    private Connection con;

    Withdraw(MainMenu mm, int userId){
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectDatabase();
                try{
                    int senderAccNo;
                    int balance;
                    int updatedBalance;
                    int withdrawAmount = Integer.parseInt(textField1.getText().strip());

                    //Getting Balance
                    String getBalanceQuery = "select balance,account_no from account where user_id=?";
                    PreparedStatement getBalance= con.prepareStatement(getBalanceQuery);
                    getBalance.setInt(1, userId);
                    ResultSet senderAccResult = getBalance.executeQuery();
                    if(senderAccResult.next()){
                        senderAccNo = senderAccResult.getInt("account_no");
                        balance = senderAccResult.getInt("balance");
                        //Updating balance
                        if((balance-withdrawAmount)>0){
                            updatedBalance = balance - withdrawAmount;
                            boolean updated = updateBalance(userId, updatedBalance, con);
                            if(updated){
                                updateTransactionHistory(userId, "Withdraw", senderAccNo, withdrawAmount, con);
                                dispose();
                                JOptionPane.showMessageDialog(Withdraw.this, "Transaction Successful!!\n"+withdrawAmount+" has been withdrawn from your account"  , "Transaction Completed", JOptionPane.INFORMATION_MESSAGE);
                                mm.setVisible(true);
                            } else{
                                JOptionPane.showMessageDialog(Withdraw.this, "Transaction failed! Please try again or contact your bank"  , "Transaction Completed", JOptionPane.ERROR_MESSAGE);
                                updateBalance(userId, balance+withdrawAmount, con);
                                dispose();
                                mm.setVisible(true);
                            }

                        } else{
                            JOptionPane.showMessageDialog(Withdraw.this, "You do not have enough funds in your account!", "Insufficient Funds", JOptionPane.ERROR_MESSAGE);
                        }
                    } else{
                        JOptionPane.showMessageDialog(Withdraw.this, "Error: Couldn't find the balance", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch(SQLException SqlQueryE){
                    JOptionPane.showMessageDialog(Withdraw.this, "Error: Database Query Error", "Database Query Error", JOptionPane.ERROR_MESSAGE);
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
        this.setContentPane(this.withdrawPanel);
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

    private static void updateTransactionHistory(int userId, String type, int sourceAccNo, int amount, Connection con){
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
            insertStatement.setNull(5, java.sql.Types.INTEGER);
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

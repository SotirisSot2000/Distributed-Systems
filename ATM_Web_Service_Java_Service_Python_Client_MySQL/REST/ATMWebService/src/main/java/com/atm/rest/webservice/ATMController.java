//Αuthor: Sotiris Sotiriou
//Date: 26/12/2021

package com.atm.rest.webservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/*
ERR1: user not found
ERR2: wrong value
ERR3: balance less than withdraw price
ERR4: wrong action
ERR5: DB error
*/

//Web Service using REST API and Java Spring Boot

@RestController
public class ATMController {
	@RequestMapping(method=RequestMethod.GET, path="/atm/balance/{customer_id}")
	public String balance(@PathVariable String customer_id) {
		String outmsg;
		//connect to database
        Connection conn;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "rootroot");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("DB Connection error");
            outmsg = "ERR5";
            return outmsg;
        } catch (ClassNotFoundException e){
            System.out.println(e.getMessage());
            outmsg = "ERR5";
            return outmsg;
        }
        
        String balanceOutput;
		try{
			Statement stmt=conn.createStatement();  
			ResultSet rs=stmt.executeQuery("select balance from customer where id = " + customer_id);
			double balance = -1.0;
			while(rs.next()){
				balance = rs.getDouble("balance");
			}
			
			if(balance >= 0.0){
				balanceOutput = Double.toString(balance);
			}
			else{
				balanceOutput = "ERR1";
			}

		} catch(SQLException e){
            System.out.println(e.getMessage());
            return "ERR5";
        }
		return balanceOutput;
	}
	
	
	@GetMapping(path="/atm/deposit/{customer_id}/{price}")
	public String deposit(@PathVariable String customer_id, @PathVariable double price) {
		String outmsg;
		//connect to database
        Connection conn;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "rootroot");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("DB Connection error");
            outmsg = "ERR5";
            return outmsg;
        } catch (ClassNotFoundException e){
            System.out.println(e.getMessage());
            outmsg = "ERR5";
            return outmsg;
        }
        
        String depositOutput;
		try{
			//update balance
			PreparedStatement prst = conn.prepareStatement("update customer set balance = balance + ? where id = ?");
			prst.setDouble(1, price);
			prst.setString(2, customer_id);
			int i = prst.executeUpdate();
	
			//get new balance
			Statement stmtDep = conn.createStatement();
			ResultSet rsDep = stmtDep.executeQuery("select balance from customer where id = " + customer_id);
			double balanceAfterDep = -1.0;
			while(rsDep.next()){
				balanceAfterDep = rsDep.getDouble("balance");
			}
	
	
			if(i == 1 && balanceAfterDep != -1.0) depositOutput = Double.toString(balanceAfterDep);
			else depositOutput = "ERR5";

		} catch(SQLException e){
			System.out.println(e.getMessage());
			return "ERR5";
		}
		return depositOutput;
	}
	
	
	@GetMapping(path="/atm/withdraw/{customer_id}/{price}")
	public String withdraw(@PathVariable String customer_id, @PathVariable double price) {
		String outmsg;
		//connect to database
        Connection conn;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "rootroot");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("DB Connection error");
            outmsg = "ERR5";
            return outmsg;
        } catch (ClassNotFoundException e){
            System.out.println(e.getMessage());
            outmsg = "ERR5";
            return outmsg;
        }
        
        String withdrawOutput;
		try{
			//check if balance is enough for withdraw
			Statement stmt2 = conn.createStatement();
			ResultSet rs2 = stmt2.executeQuery("select balance from customer where id = " + customer_id);
			double balanceWithdraw = -1.0;
			while(rs2.next()){
				balanceWithdraw = rs2.getDouble("balance");
			}
			if(balanceWithdraw == -1.0){
				return "ERR1";
			}
			else if(balanceWithdraw < price){
				return "ERR3";
			}

			//update balance
			PreparedStatement prst2 = conn.prepareStatement("update customer set balance = balance - ? where id = ?");
			prst2.setDouble(1, price);
			prst2.setString(2, customer_id);
			int rows = prst2.executeUpdate();
	
	
			//get new balance
			Statement stmtWithdraw = conn.createStatement();
			ResultSet rsWithdraw = stmtWithdraw.executeQuery("select balance from customer where id = " + customer_id);
			double balanceAfterWithdraw = -1.0;
			while(rsWithdraw.next()){
				balanceAfterWithdraw = rsWithdraw.getDouble("balance");
			}
	
			//check if query result is ok
			if(rows == 1 && balanceAfterWithdraw != -1.0) withdrawOutput = Double.toString(balanceAfterWithdraw);
			else return "ERR5";

		} catch (SQLException e){
			System.out.println(e.getMessage());
			return "ERR5";
		}

		return withdrawOutput;
	}

}

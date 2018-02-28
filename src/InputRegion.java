import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;


public class InputRegion extends InputPanel {
	public InputRegion(ElectionData data) {
		super(data, "Number of Regions:");
	}
	
	protected void createDTM(){
		int pSize = electionData.getParties().size();
		int rSize = electionData.getRegions().size();
		Object[] tableColumns = new String[pSize + 4];
		tableColumns[0] = "Province";
		tableColumns[1] = "Region";
		tableColumns[2] = "Population";
		tableColumns[3] = "Seats";
		for(int p = 0; p < pSize; p++){
			tableColumns[p+4] = electionData.getParty(p).getName();
		}
		Object[][] tableData = makeTableData(rSize);
		dtm = new DefaultTableModel(tableData,tableColumns){
			//lock column data types
			@Override
			public Class getColumnClass(int column) {
				switch (column) {
					case 0:
						return String.class;
					case 1:
						return String.class;
					case 2:
						return Long.class;
					case 3:
						return Integer.class;
					default:
						return Double.class;
				}
            }
        };
	}
	
	protected Object[][] makeTableData(int rows){
		int regionCount = electionData.getRegions().size();
		int partyCount = electionData.getParties().size();
		
		Object[][] tableData = new Object[rows][4+partyCount];
		for(int i = 0; i < rows; i++){
			if(i >= regionCount){
				tableData[i][2] = 0L;
				tableData[i][3] = 0;
				for(int j = 0; j < partyCount; j++){
					tableData[i][4+j] = 0.0;
				}
			}
			else{
				Region r = electionData.getRegion(i);
				if(r == null){
					tableData[i][2] = 0L;
					tableData[i][3] = 0;
					for(int j = 0; j < partyCount; j++){
						tableData[i][4+j] = 0.0;
					}
				}
				else{
					tableData[i][0] = r.getProvince().getName();
					tableData[i][1] = r.getName();
					tableData[i][2] = r.getPopulation();
					tableData[i][3] = r.getSeats();
					for(int j = 0; j < partyCount; j++){
						if(j >= partyCount){
							tableData[i][4+j] = 0.0;
						}
						else{
							Party p = electionData.getParty(j);
							if(p == null){
								tableData[i][4+j] = 0.0;
							}
							else{
								Party op = electionData.getConversion(p);
								if(op == null){
									tableData[i][4+j] = 0.0;
								}
								else{
									tableData[i][4+j] = r.getSupport(op)*100.0;
								}
							}
						}
					}
				}
			}
		}
		return tableData;
	}
	
	public void changeTableRows(){
		int rows = Integer.parseInt(sizeCounter.getText());
		int partyCount = electionData.getParties().size();
		if(dtm != null){
			while(rows < dtm.getRowCount()){
				dtm.removeRow(dtm.getRowCount()-1);
			}
			while(rows > dtm.getRowCount()){
				Object[] newRow = new Object[partyCount+4];
				newRow[2] = 0L;
				newRow[3] = 0;
				for(int p = 0; p < partyCount; p++){
					newRow[p+4] = 0.0;
				}
				dtm.addRow(newRow);
			}
			dtm.fireTableDataChanged();
		}
	}

	public void updateElectionData() {
		List<Province> newProvinces = new ArrayList<>();
		List<Region> newRegions = new ArrayList<>();
		int partyCount = electionData.getParties().size();
		for(int i = 0; i < dtm.getRowCount(); i++){
			String pName = (String)table.getValueAt(i,0);
			String name = (String)table.getValueAt(i,1);
			long population = (Long)table.getValueAt(i,2);
			int seats = (Integer)table.getValueAt(i,3);
			Map<Party,Double> support = new HashMap<>();
			for(int j = 0; j < partyCount; j++){
				support.put(electionData.getParty(j), (Double)table.getValueAt(i, j+4));
			}
			//find region's province and add it to the region
			boolean provFound = false;
			for(Province province : newProvinces){
				if(province.getName().equals(pName)){
					newRegions.add(new Region(name,population,seats,province,support));
					provFound = true;
				}
			}
			if(!provFound){
				Province newProv = new Province(pName);
				newProvinces.add(newProv);
				newRegions.add(new Region(name,population,seats,newProv,support));
			}
		}
		
		electionData.setProvinces(newProvinces);
		electionData.setRegions(newRegions);
	}
}

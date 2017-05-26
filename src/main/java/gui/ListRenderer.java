package gui;

import javax.swing.* ;
import java.awt.* ;

class ListRenderer implements ListCellRenderer<String>
{
	JLabel label ;
	private Font font;

	private DefaultListCellRenderer defaultRenderer;

	public ListRenderer()
	{
		font = new Font("",Font.BOLD | Font.ITALIC,12);
		defaultRenderer = new DefaultListCellRenderer();
	}


	@Override
	public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
		label = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,isSelected, cellHasFocus);
		if(value.startsWith("@@")){
			//value = value.substring(1);
			label.setText(value.substring(1));
			label.setFont(font);
		}
		
		return label ;
	}
}
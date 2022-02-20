

import javax.microedition.lcdui.Form;

public abstract class ModelForm extends Form implements Runnable {

	public ModelForm(String title) {
		super(title);
	}

	public abstract AbstractModel getModel();

	public abstract void setFormContainer(Form form);
	
}

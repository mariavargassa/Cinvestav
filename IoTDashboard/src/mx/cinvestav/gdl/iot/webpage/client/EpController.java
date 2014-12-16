package mx.cinvestav.gdl.iot.webpage.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mx.cinvestav.gdl.iot.webpage.dto.ControllerDTO;
import mx.cinvestav.gdl.iot.webpage.dto.ControllerPropertyDTO;
import mx.cinvestav.gdl.iot.webpage.dto.IoTPropertyDTO;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EpController extends IoTEntryPoint
{
	private DialogBox dbWait = new DialogBox();

	private String idController;
	private Button saveProperty = new Button("Save");
	private Button cancelProperty = new Button("Cancel");

	private DialogBox dialogBox = new DialogBox();
	private Button btDialogClose = new Button("Close");
	private Button btDialogError = new Button("Close");
	private VerticalPanel dialogPanel = new VerticalPanel();
	private Label lbDialogBox = new Label();

	final DecoratedPopupPanel popup = new DecoratedPopupPanel(true);

	private ListBox listIdProperty = new ListBox(true);
	private ListBox listNameProperty = new ListBox(true);
	private ListBox listValueProperty = new ListBox(true);
	private ListBox listActiveProperty = new ListBox(true);

	private TextBox name = new TextBox();
	private TextBox value = new TextBox();
	private CheckBox active = new CheckBox();

	private VerticalPanel formPanel = new VerticalPanel();

	private Button btSaveController = new Button("Save Controller");
	private Button btCancelController = new Button("Cancel");
	private HorizontalPanel buttonsPanel = new HorizontalPanel();

	private FlexTable tableFields = new FlexTable();
	private TextBox tbId = new TextBox();
	private TextBox tbName = new TextBox();
	private TextBox tbDescription = new TextBox();
	private TextBox tbLocation = new TextBox();

	private VerticalPanel propertyPanel = new VerticalPanel();
	private Label lbProperty = new Label();
	private FlexTable tableProperty = new FlexTable();

	private Button btAddProperty = new Button("Add");
	private ArrayList<String> property = new ArrayList<String>();

	private static final EntityStoreServiceAsync entityService = GWT.create(EntityStoreService.class);

	@Override
	public void continueModuleLoad()
	{
		tableFields.setText(0, 0, "Id: ");
		tableFields.setWidget(0, 1, tbId);
		tableFields.setText(1, 0, "Name: ");
		tableFields.setWidget(1, 1, tbName);
		tableFields.setText(2, 0, "Description: ");
		tableFields.setWidget(2, 1, tbDescription);
		tableFields.setText(3, 0, "Location: ");
		tableFields.setWidget(3, 1, tbLocation);

		tbId.setEnabled(false);

		tableProperty.setText(0, 0, "ID");
		tableProperty.setText(0, 1, "Name");
		tableProperty.setText(0, 2, "Value");
		tableProperty.setText(0, 3, "Active");
		tableProperty.setText(0, 4, "");
		tableProperty.getCellFormatter().addStyleName(0, 0, "headerTableProperty");
		tableProperty.getCellFormatter().addStyleName(0, 1, "headerTableProperty");
		tableProperty.getCellFormatter().addStyleName(0, 2, "headerTableProperty");
		tableProperty.getCellFormatter().addStyleName(0, 3, "headerTableProperty");
		tableProperty.getCellFormatter().addStyleName(0, 4, "headerTableProperty");
		tableProperty.addStyleName("tableProperty");
		tableProperty.setCellPadding(3);

		lbProperty.setText("Property");
		lbProperty.setStyleName("lbProperty");

		propertyPanel.add(lbProperty);
		propertyPanel.add(btAddProperty);
		propertyPanel.add(tableProperty);

		listIdProperty.setVisible(false);
		listNameProperty.setVisible(false);
		listValueProperty.setVisible(false);
		listActiveProperty.setVisible(false);
		formPanel.add(listIdProperty);
		formPanel.add(listNameProperty);
		formPanel.add(listValueProperty);
		formPanel.add(listActiveProperty);
		formPanel.add(tableFields);
		formPanel.add(propertyPanel);

		buttonsPanel.add(btSaveController);
		buttonsPanel.add(btCancelController);
		formPanel.add(buttonsPanel);
		formPanel.setCellHorizontalAlignment(buttonsPanel, HasHorizontalAlignment.ALIGN_RIGHT);

		RootPanel.get("formContainer").add(formPanel);

		btDialogClose.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				dialogBox.hide();
				Window.Location.replace("wpControllers.jsp");
			}
		});

		btDialogError.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				dialogBox.hide();
			}
		});

		btAddProperty.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				btAddProperty.setEnabled(false);
				addPropertyRow();
			}
		});

		btSaveController.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				saveControllerAction();
			}
		});

		btCancelController.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				Window.Location.replace("wpControllers.jsp");
			}
		});

		saveProperty.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				btAddProperty.setEnabled(true);
				savePropertyAction();
			}
		});

		cancelProperty.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				btAddProperty.setEnabled(true);
				tableProperty.removeRow(tableProperty.getRowCount() - 1);
			}
		});

		// We need to retrieve the id controller to update
		idController = Window.Location.getParameter("idController");
		if (idController != null)
		{
			updateControllerAction();
		}
	}

	private void updateControllerAction()
	{
		int id = Integer.parseInt(idController);
		showDialogWait();
		entityService.getEntity(new ControllerDTO(), id, new AsyncCallback<List<ControllerDTO>>()
		{
			@Override
			public void onFailure(Throwable caught)
			{
				dbWait.hide();
				//TODO:
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(List<ControllerDTO> result)
			{
				fillControllerData(result);
			}
		});
	}

	private void fillControllerData(List<ControllerDTO> result)
	{
		ControllerDTO c = result.get(0);
		tbId.setText(c.getId() + "");
		tbName.setText(c.getName());
		tbDescription.setText(c.getDescription());
		tbLocation.setText(c.getLocation());
		entityService.getProperties(new ControllerPropertyDTO(), c.getId(),
				new AsyncCallback<List<ControllerPropertyDTO>>()
				{
					@Override
					public void onFailure(Throwable caught)
					{
						dbWait.hide();
						//TODO:
						Window.alert(caught.getMessage());
					}

					@Override
					public void onSuccess(List<ControllerPropertyDTO> resultP)
					{
						dbWait.hide();
						for (int i = 0; i < resultP.size(); i++)
						{
							tableProperty.setText(i + 1, 0, resultP.get(i).getId() + "");
							tableProperty.setText(i + 1, 1, resultP.get(i).getName());
							tableProperty.setText(i + 1, 2, resultP.get(i).getValue());
							CheckBox cb = new CheckBox();
							cb.setValue(resultP.get(i).isActive());
							cb.setEnabled(false);
							tableProperty.setWidget(i + 1, 3, cb);

							listIdProperty.addItem(resultP.get(i).getId() + "");
							listNameProperty.addItem(resultP.get(i).getName());
							listValueProperty.addItem(resultP.get(i).getValue());
							listActiveProperty.addItem(resultP.get(i).isActive() + "");

							final String id = resultP.get(i).getId() + "";
							property.add(id);

							final Button saveEditProperty = new Button("Save");
							final Button removeProperty = new Button("Remove");
							final Button editProperty = new Button("Edit");
							final Button cancelEditProperty = new Button("Cancel");

							saveEditProperty.setVisible(false);
							removeProperty.setVisible(true);
							editProperty.setVisible(true);
							cancelEditProperty.setVisible(false);

							removeProperty.addClickHandler(new ClickHandler()
							{
								public void onClick(ClickEvent event)
								{
									removePropertyAction(id);
								}
							});

							editProperty.addClickHandler(new ClickHandler()
							{
								public void onClick(ClickEvent event)
								{
									saveEditProperty.setVisible(true);
									removeProperty.setVisible(false);
									editProperty.setVisible(false);
									cancelEditProperty.setVisible(true);

									int editRow = property.indexOf(id);
									name.setText(listNameProperty.getItemText(editRow));
									value.setText(listValueProperty.getItemText(editRow));

									if (Boolean.parseBoolean(listActiveProperty.getItemText(editRow)))
									{
										active.setValue(true);
									}
									else
									{
										active.setValue(false);
									}

									tableProperty.setWidget(editRow + 1, 1, name);
									tableProperty.setWidget(editRow + 1, 2, value);
									tableProperty.setWidget(editRow + 1, 3, active);
								}

							});

							saveEditProperty.addClickHandler(new ClickHandler()
							{
								public void onClick(ClickEvent event)
								{
									saveEditProperty.setVisible(false);
									removeProperty.setVisible(true);
									editProperty.setVisible(true);
									cancelEditProperty.setVisible(false);

									int editRow = property.indexOf(id);

									final String symboln = name.getText();
									final String symbolv = value.getText();
									CheckBox symbola = new CheckBox();

									if (active.getValue() == true)
									{
										symbola.setValue(true);
									}
									else
									{
										symbola.setValue(false);
									}

									symbola.setEnabled(false);

									if (symboln.length() > 45 || symboln.length() == 0)
									{
										showInformationDialog("Error",
												"The name must not have empty and have less than 45 characters",
												btDialogError);
										return;
									}

									if (symbolv.length() > 45)
									{
										showInformationDialog("Error", "The value have less than 45 characters",
												btDialogError);
										return;
									}

									tableProperty.setText(editRow + 1, 1, symboln);
									tableProperty.setText(editRow + 1, 2, symbolv);
									tableProperty.setWidget(editRow + 1, 3, symbola);

									listNameProperty.setItemText(editRow, symboln);
									listValueProperty.setItemText(editRow, symbolv);
									listActiveProperty.setItemText(editRow, symbola.getValue() + "");

									name.setText("");
									value.setText("");
									active.setValue(false);

								}
							});

							cancelEditProperty.addClickHandler(new ClickHandler()
							{
								public void onClick(ClickEvent event)
								{
									saveEditProperty.setVisible(false);
									removeProperty.setVisible(true);
									editProperty.setVisible(true);
									cancelEditProperty.setVisible(false);

									int editRow = property.indexOf(id);
									tableProperty.setText(editRow + 1, 1, name.getText());
									tableProperty.setText(editRow + 1, 2, value.getText());
									tableProperty.setWidget(editRow + 1, 3, active);

									name.setText("");
									value.setText("");
									active.setValue(false);

								}
							});

							HorizontalPanel buttonsPanel = new HorizontalPanel();
							buttonsPanel.add(editProperty);
							buttonsPanel.add(saveEditProperty);
							buttonsPanel.add(cancelEditProperty);
							buttonsPanel.add(removeProperty);

							tableProperty.setWidget(i + 1, 4, buttonsPanel);
						}
					}
				});
	}

	private void removePropertyAction(String id)
	{
		int deleteP = Integer.parseInt(listIdProperty.getItemText(property.indexOf(id)));
		showDialogWait();
		entityService.deleteProperty(new ControllerPropertyDTO(), deleteP, new AsyncCallback<Void>()
		{

			@Override
			public void onFailure(Throwable caught)
			{
				//TODO
				dbWait.hide();
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result)
			{
				dbWait.hide();
				Window.Location.reload();
			}
		});
	}

	private void saveControllerAction()
	{
		ControllerDTO c = new ControllerDTO();
		if (idController != null)
		{
			c.setId(Integer.parseInt(idController));
		}
		c.setName(tbName.getText());
		c.setDescription(tbDescription.getText());
		c.setLocation(tbLocation.getText());
		Collection<IoTPropertyDTO> props = new ArrayList<>();
		for (int i = 0; i < listNameProperty.getItemCount(); i++)
		{
			IoTPropertyDTO prop = new ControllerPropertyDTO();
			if (idController != null)
			{
				prop.setParentId(c.getId());
			}
			String idProp = listIdProperty.getItemText(i);
			if (!"".equals(idProp))
			{
				prop.setId(Integer.parseInt(idProp));
			}
			else
			{
				prop.setId(null);
			}
			prop.setName(listNameProperty.getItemText(i));
			prop.setValue(listValueProperty.getItemText(i));
			prop.setActive(Boolean.valueOf(listActiveProperty.getItemText(i)));
			props.add(prop);
		}

		showDialogWait();
		entityService.storeEntity(c, props, new AsyncCallback<Void>()
		{
			@Override
			public void onSuccess(Void result)
			{
				dbWait.hide();
				showInformationDialog("Information", "Controller succesfully stored", btDialogClose);
			}

			@Override
			public void onFailure(Throwable caught)
			{
				//TODO
				dbWait.hide();
				Window.alert(caught.getMessage());
			}
		});
	}

	private void showInformationDialog(String title, String message, Button btDialogClose)
	{
		dialogBox.setAnimationEnabled(true);
		dialogBox.center();
		dialogBox.setGlassEnabled(true);
		dialogBox.setText(title);
		lbDialogBox.setText(message);
		dialogPanel.add(lbDialogBox);
		dialogPanel.setCellHorizontalAlignment(lbDialogBox, HasHorizontalAlignment.ALIGN_CENTER);
		dialogPanel.add(btDialogClose);
		dialogPanel.setCellHorizontalAlignment(btDialogClose, HasHorizontalAlignment.ALIGN_CENTER);
		dialogBox.add(dialogPanel);
		dialogBox.show();
	}

	private void addPropertyRow()
	{
		int row = tableProperty.getRowCount();
		tableProperty.setText(row, 0, " ");
		tableProperty.setWidget(row, 1, name);
		tableProperty.setWidget(row, 2, value);
		tableProperty.setWidget(row, 3, active);
		HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.add(saveProperty);
		buttonPanel.add(cancelProperty);
		tableProperty.setWidget(row, 4, buttonPanel);
	}

	private void savePropertyAction()
	{
		tableProperty.removeRow(tableProperty.getRowCount() - 1);

		final String symboln = name.getText();
		final String symbolv = value.getText();
		CheckBox symbola = new CheckBox();

		if (active.getValue() == true)
		{
			symbola.setValue(true);
		}
		else
		{
			symbola.setValue(false);
		}

		symbola.setEnabled(false);

		if (symboln.length() > 45 || symboln.length() == 0)
		{
			showInformationDialog("Error", "The name must not have empty and have less than 45 characters",
					btDialogError);
			return;
		}

		if (symbolv.length() > 45)
		{
			showInformationDialog("Error", "The value have less than 45 characters", btDialogError);
			return;
		}

		name.setText("");
		value.setText("");
		active.setValue(false);

		int row = tableProperty.getRowCount();
		tableProperty.setText(row, 0, "");
		tableProperty.setText(row, 1, symboln);
		tableProperty.setText(row, 2, symbolv);
		tableProperty.setWidget(row, 3, symbola);

		listIdProperty.addItem("");
		listNameProperty.addItem(symboln);
		listValueProperty.addItem(symbolv);
		listActiveProperty.addItem(symbola.getValue() + "");
		property.add(symboln);

		final Button saveEditProperty = new Button("Save");
		final Button removeProperty = new Button("Remove");
		final Button editProperty = new Button("Edit");
		final Button cancelEditProperty = new Button("Cancel");

		saveEditProperty.setVisible(false);
		removeProperty.setVisible(true);
		editProperty.setVisible(true);
		cancelEditProperty.setVisible(false);

		removeProperty.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				int removedIndex = property.indexOf(symboln);
				property.remove(removedIndex);
				tableProperty.removeRow(removedIndex + 1);

				listIdProperty.removeItem(removedIndex);
				listNameProperty.removeItem(removedIndex);
				listValueProperty.removeItem(removedIndex);
				listActiveProperty.removeItem(removedIndex);
			}
		});

		editProperty.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				saveEditProperty.setVisible(true);
				removeProperty.setVisible(false);
				editProperty.setVisible(false);
				cancelEditProperty.setVisible(true);
				btAddProperty.setEnabled(false);

				int editRow = property.indexOf(symboln);
				name.setText(listNameProperty.getItemText(editRow));
				value.setText(listValueProperty.getItemText(editRow));

				if (Boolean.parseBoolean(listActiveProperty.getItemText(editRow)))
				{
					active.setValue(true);
				}
				else
				{
					active.setValue(false);
				}

				tableProperty.setWidget(editRow + 1, 1, name);
				tableProperty.setWidget(editRow + 1, 2, value);
				tableProperty.setWidget(editRow + 1, 3, active);
			}

		});

		saveEditProperty.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				saveEditProperty.setVisible(false);
				removeProperty.setVisible(true);
				editProperty.setVisible(true);
				cancelEditProperty.setVisible(false);
				btAddProperty.setEnabled(true);

				int editRow = property.indexOf(symboln);

				final String symboln = name.getText();
				final String symbolv = value.getText();
				CheckBox symbola = new CheckBox();

				if (active.getValue() == true)
				{
					symbola.setValue(true);
				}
				else
				{
					symbola.setValue(false);
				}

				symbola.setEnabled(false);

				if (symboln.length() > 45 || symboln.length() == 0)
				{
					showInformationDialog("Error", "The name must not have empty and have less than 45 characters",
							btDialogError);
					return;
				}

				if (symbolv.length() > 45)
				{
					showInformationDialog("Error", "The value have less than 45 characters", btDialogError);
					return;
				}

				tableProperty.setText(editRow + 1, 1, symboln);
				tableProperty.setText(editRow + 1, 2, symbolv);
				tableProperty.setWidget(editRow + 1, 3, symbola);

				listNameProperty.setItemText(editRow, symboln);
				listValueProperty.setItemText(editRow, symbolv);
				listActiveProperty.setItemText(editRow, symbola.getValue() + "");

				name.setText("");
				value.setText("");
				active.setValue(false);

			}
		});

		cancelEditProperty.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				btAddProperty.setEnabled(true);
				int editRow = property.indexOf(symboln);
				tableProperty.setText(editRow + 1, 1, name.getText());
				tableProperty.setText(editRow + 1, 2, value.getText());
				tableProperty.setWidget(editRow + 1, 3, active);

				name.setText("");
				value.setText("");
				active.setValue(false);

			}
		});

		HorizontalPanel buttonsPanel = new HorizontalPanel();
		buttonsPanel.add(editProperty);
		buttonsPanel.add(saveEditProperty);
		buttonsPanel.add(cancelEditProperty);
		buttonsPanel.add(removeProperty);

		tableProperty.setWidget(row, 4, buttonsPanel);
	}

	public void showDialogWait()
	{

		dbWait.setAnimationEnabled(true);
		dbWait.setGlassEnabled(true);
		dbWait.setModal(true);
		dbWait.center();

		VerticalPanel dialogContents = new VerticalPanel();

		dialogContents.setSpacing(4);

		Image image = new Image();

		image.setUrl(GWT.getHostPageBaseURL() + "images/loading2.gif");

		dialogContents.add(image);
		dialogContents.setCellHorizontalAlignment(image, HasHorizontalAlignment.ALIGN_CENTER);

		dbWait.setWidget(dialogContents);
		dbWait.show();

	}

}

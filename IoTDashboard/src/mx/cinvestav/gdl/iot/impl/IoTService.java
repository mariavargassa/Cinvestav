package mx.cinvestav.gdl.iot.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.persistence.EntityManager;

import mx.cinvestav.gdl.iot.cloudclient.Measure;
import mx.cinvestav.gdl.iot.cloudclient.SensorData;
import mx.cinvestav.gdl.iot.cloudclient.UpdateDataRequest;
import mx.cinvestav.gdl.iot.cloudclient.UpdateDataResponse;
import mx.cinvestav.gdl.iot.dao.DAO;
import mx.cinvestav.gdl.iot.dao.SmartThing;
import mx.cinvestav.gdl.iot.validation.UpdateRequestValidator;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.utils.SystemProperty;

/**
 * Defines v1 of a IoT API, which provides simple update methods.
 */
@Api(name = "iotService", version = "v1")
public class IoTService
{
	private final String DATABASE_ENDPOINT;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public IoTService()
	{
		String url = null;
		try
		{
			if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production)
			{
				Class.forName("com.mysql.jdbc.GoogleDriver");
				url = "jdbc:google:mysql://weighty-utility-768:ihc/cityfarm?user=root";
			}
			else
			{
				Class.forName("com.mysql.jdbc.Driver");
				url = "jdbc:mysql://173.194.253.85:3306/cityfarm?user=root&password=root";
			}
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "Unexpected exception initializing iot service", e);
		}
		DATABASE_ENDPOINT = url;
	}

	@ApiMethod(name = "updateData", httpMethod = "post")
	public UpdateDataResponse updateData(UpdateDataRequest request) throws NotFoundException
	{
		UpdateDataResponse res = new UpdateDataResponse();
		try
		{
			if (request != null)
			{
				int status = 0;
				String validationResult = UpdateRequestValidator.validate(request);
				if (validationResult == null || "".equals(validationResult))
				{
					String controllerId = request.getControllerId();
					String smartId = request.getSmartThingId();
					
					PersistenceManager pm = DAO.getPersistenceManager();
					Transaction tx = pm.currentTransaction();
					try
					{
						tx.begin();
						SensorData[] sensorData = request.getSensorData();
						for (SensorData data : sensorData)
						{
							String sensorId = data.getSensorId();
							//validar sensor-cotrolador
							Measure[] measures = data.getMeasures();
							for (Measure m : measures)
							{
								String data2 = m.getData();
								String time = m.getTime();

								//String statement = "INSERT INTO sensor (idcontrolador, ) VALUES( ? , ? )";
								//PreparedStatement stmt = conn.prepareStatement(statement);
								//stmt.setString(1, fname);
								//stmt.setString(2, content);
								int success = 2;
								//success = stmt.executeUpdate();
							}
						}
						tx.commit();
					}
					catch (ArrayIndexOutOfBoundsException e)
					{

					}
					finally
					{
						if (tx.isActive())
						{
							//error, we need to rollback
							tx.rollback();
						}
						pm.close();
					}

					res.setMessage("OK" + status);
					res.setStatus(200);
				}
				else
				{
					res.setMessage("Invalid request:" + validationResult);
					res.setStatus(400);
				}
			}
			else
			{
				res.setMessage("Empty request");
				res.setStatus(404);
			}
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "Unexpected exception in update data", e);
			res.setMessage(e.getMessage());
			res.setStatus(500);
		}
		return res;
	}
}
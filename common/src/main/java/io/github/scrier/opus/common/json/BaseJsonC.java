package io.github.scrier.opus.common.json;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseJsonC {
	
	private static Logger log = LogManager.getLogger(BaseJsonC.class);

	private String source;
	private long valuesModified;
	
	public BaseJsonC(String source) {
		setSource(source);
		resetValuesModified();
	}
	
	/**
	 * Try to parse objects to source.
	 * @return boolean
	 */
	public boolean tryParseToJson() {
		log.trace("tryParseToJson()");
		boolean retValue = true;
		try {
			parseToJson();
		} catch (Exception e) {
			log.error("Exception from parseToJson method.", e);
			retValue = false;
		}
		return retValue;
	}
	
	/**
	 * Method to implement parsing to class from source.
	 * @throws Exception exception thrown.
	 */
	public abstract void parseToJson() throws Exception;
	
	/**
	 * Try to parse source to objects
	 * @return boolean
	 */
	public boolean tryParseFromJson() {
		log.trace("tryParseFromJson()");
		boolean retValue = true;
		try {
			parseFromJson();
		} catch (Exception e) {
			log.error("Exception from parseFromJson method.", e);
			retValue = false;
		}
		return retValue;
	}
	
	/**
	 * Method to implement parsing from source to class.
	 * @throws Exception exception thrown.
	 */
	public abstract void parseFromJson() throws Exception;

	/**
	 * @return the source
	 */
  public String getSource() {
	  return source;
  }

	/**
	 * @param source the source to set
	 */
  private void setSource(String source) {
	  this.source = source;
  }
  
  /**
   * Method to check if a value is modified.
   * @param value
   * @return boolean 
   */
	public boolean isValueModified(long value) {
		return (value & getValuesModified()) > 0; 
	}
  
	/**
	 * Method to set values modified.
	 * @param value long
	 */
	protected void addValueModified(long value) {
		setValuesModified(getValuesModified() | value); 
	}
	
	/**
	 * Method to reset values modified.
	 */
	protected void resetValuesModified() {
		setValuesModified(0L); 
	}
	
	/**
	 * @return the valuesModified
	 */
	private long getValuesModified() {
		return valuesModified;
	}

	/**
	 * @param valuesModified the valuesModified to set
	 */
	private void setValuesModified(long valuesModified) {
		this.valuesModified = valuesModified;
	}
	
}

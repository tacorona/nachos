

// -- FUNCTIONS --

class Functions{

    def context
    def testRunner
    def log
    
	def errorLines = []

	//TestFunction - Used to confirm initialisation of this file
	def printCall(){
		log.info "Function.groovy is loaded"
	}

	//Initiate folders
	def checkAndCreateFolder(name, path, date){
		File directory = new File(path + "\\" + name + "\\" + date);
		if (! directory.exists()){
		directory.mkdirs();
		}
	}

	// Find the column number for given column name
	def getColumn(name, ws, totalColumns){
		def row = ws.getRow(0)
		def colmnNumber
		for (def j=1; j<totalColumns; j++) {
			def cell = row.getCell(j)
			//log.info cell.toString()
			if (cell.toString() == name){
			colmnNumber = j
			break;
			}
		}
		return colmnNumber
	}

	//Return the value of a cell, rather than the formula and no matter what type
	def getCellValue(cell){
		def value
		if (null != cell) {
			// The .getCellType() methode returns a numeric value or the name of the type depending on the POI version used; 
			// This is coverd in the switch-case.
			switch (cell.getCellType()){
				case "BOOLEAN":
				case 4:
					value = cell.getBooleanCellValue();
					break;
				case "FORMULA":
				case 2:
					switch (cell.getCachedFormulaResultType()) {
						case "BOOLEAN":
						case 4:
							value = cell.getBooleanCellValue();
							break;
						case "NUMERIC":
						case 0:
							value = cell.getNumericCellValue();
							break;
						case "STRING":
						case 1:
							value = cell.getRichStringCellValue();
							break;
					}
					break;
				case "STRING":
				case 1:
					value = cell.getRichStringCellValue();
					break;
				case "NUMERIC":
				case 0:
					value = cell.getNumericCellValue();
					break;
				default:
					value = ""
					break;
			}
		}
		else{
			value = ""
		}
		return value.toString()
	}

	//check the difference between 2 values: <=0.05 is acceptable
	def checkValues(val1,val2){
		def float dec1 
		def float dec2
		try{
			dec1 = Float.parseFloat(val1);
			dec2 = Float.parseFloat(val2);
		}
		catch(Exception e){
			log.info "EXCEPTION val1: " + val1
			log.info "EXCEPTION val2: " +val2
		}
		//make the value absolute and truncate at 2 decimals
		def difference = Math.abs(dec1 - dec2).trunc(2)
		if (difference > 0.05){
			return false
		}else{
			return true
		}
	}

	//create Error Log Text from given arguments
	def createErrorLogText(message, expected, result){
		def errorText = []
		errorText << message + "\n"
		errorText << "Expected: " + expected + "\n"
		errorText << "Result: " + result + "\n"
		errorText << "----------------------------------------\n\n"
		
		return errorText
	}

	def rowPassed(i, row){
		log.info "  Row " + (i+1) + " - PASS"
		row.createCell(0).setCellValue("PASS");
	}

	def rowFailed(errorLog, i, projectPath, date, row){
		def text = "  Row " + (i+1) + " - FAIL"
		log.error text
		errorLines << text + "\n"
		writeListAsLinesToLog(errorLog, i, projectPath, date);
		row.createCell(0).setCellValue("FAIL");
	}


	//Save xls File
	def saveFile(name, path, wb){
		try{
			FileOutputStream fileOut = new FileOutputStream(path + "//" + name + ".xls");
			wb.write(fileOut);
			fileOut.close();
			log.info name + ".xls updated and saved"
		}
		catch(FileNotFoundException e){
			//when the file is open, save in new file
			log.error " xls FILE OPEN - Saving in new file..."
			saveFile(name + "_NEW", path, wb);
		}
	}

	//Write all lines of the error list to the error-log-file
	def writeListAsLinesToLog(list, i, path, date){
		def errorLogFile = new File(path + "\\Fails\\" + date + "\\row " + (i+1) +".txt");
		for (line in list){
			log.info line.toString()
			errorLogFile.append (line.toString());
		}
	}

	def finishTestSuite(excelName, projectPath, wb){
		//Save file - but only if not run on devOps
		if (projectPath != "D:\\a\\1\\s"){
			saveFile(excelName, projectPath, wb);
		}
		
		//Throw error to force the state of the pipeline-TestCase to FAILED
		if (errorLines.size() > 0) {
			throw new Exception("Following testcase(s) FAILED: \n" + errorLines)
		}
	}
}

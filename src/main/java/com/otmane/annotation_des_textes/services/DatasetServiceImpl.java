@Override
public void ParseDataset(Dataset dataset) {
    final int MAX_ROWS = 1000;
    final int BATCH_SIZE = 25;

    String filename = dataset.getFilePath();
    if (filename == null || filename.isEmpty()) {
        throw new IllegalArgumentException("Dataset has no associated file");
    }

    Path filePath = Paths.get(filename);
    if (!Files.exists(filePath)) {
        File resourceFile = new File(filename);
        if (resourceFile.exists()) {
            filePath = resourceFile.toPath();
        } else {
            throw new RuntimeException("File not found: " + filePath.toString());
        }
    }

    String fileExtension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

    try {
        if ("csv".equals(fileExtension)) {
            parseCSVFile(filePath, dataset, MAX_ROWS, BATCH_SIZE);
        } else if ("xlsx".equals(fileExtension) || "xls".equals(fileExtension)) {
            parseExcelFile(filePath, dataset, MAX_ROWS, BATCH_SIZE);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + fileExtension);
        }
    } catch (IOException e) {
        throw new RuntimeException("Error reading file: " + e.getMessage(), e);
    }
}

private void parseCSVFile(Path filePath, Dataset dataset, int maxRows, int batchSize) throws IOException {
    List<CoupleText> batch = new ArrayList<>();
    int rowCount = 0;

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
        // Skip header row
        reader.readLine();

        String line;
        while ((line = reader.readLine()) != null && rowCount < maxRows) {
            String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); // Gestion correcte des virgules dans les guillemets
            
            if (parts.length >= 2) {
                String text1 = parts[0].trim();
                String text2 = parts[1].trim();
                
                // Enlever les guillemets si présents
                text1 = text1.replaceAll("^\"|\"$", "");
                text2 = text2.replaceAll("^\"|\"$", "");

                CoupleText couple = new CoupleText();
                couple.setTexte1(text1);
                couple.setTexte2(text2);
                couple.setDataset(dataset);

                batch.add(couple);
                rowCount++;

                if (batch.size() == batchSize) {
                    coupleTextService.saveBatch(batch);
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) {
            coupleTextService.saveBatch(batch);
        }
    }
}

private void parseExcelFile(Path filePath, Dataset dataset, int maxRows, int batchSize) throws IOException {
    try (InputStream fileInputStream = new FileInputStream(filePath.toFile());
         Workbook workbook = WorkbookFactory.create(fileInputStream)) {

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        int rowCount = 0;
        List<CoupleText> batch = new ArrayList<>();

        // Skip header row
        if (rowIterator.hasNext()) rowIterator.next();

        while (rowIterator.hasNext() && rowCount < maxRows) {
            Row row = rowIterator.next();
            Cell text1Cell = row.getCell(0);
            Cell text2Cell = row.getCell(1);

            if (text1Cell == null || text2Cell == null) continue;

            String text1 = text1Cell.getStringCellValue().trim();
            String text2 = text2Cell.getStringCellValue().trim();

            CoupleText couple = new CoupleText();
            couple.setTexte1(text1);
            couple.setTexte2(text2);
            couple.setDataset(dataset);

            batch.add(couple);
            rowCount++;

            if (batch.size() == batchSize) {
                coupleTextService.saveBatch(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            coupleTextService.saveBatch(batch);
        }
    }
} 
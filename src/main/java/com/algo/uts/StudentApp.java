package com.algo.uts;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;

public class StudentApp extends Application {
    
    // Model class untuk siswa
    public static class Student implements Comparable<Student> {
        private String npm;
        private String nama;
        private double nilaiTugas;
        private double nilaiUTS;
        private double nilaiUAS;
        private double nilaiAkhir;
        
        public Student(String npm, String nama, double nilaiTugas, double nilaiUTS, double nilaiUAS) {
            this.npm = npm;
            this.nama = nama;
            this.nilaiTugas = nilaiTugas;
            this.nilaiUTS = nilaiUTS;
            this.nilaiUAS = nilaiUAS;
            this.hitungNilaiAkhir();
        }
        
        private void hitungNilaiAkhir() {
            // Rumus nilai akhir: 30% Tugas + 30% UTS + 40% UAS
            this.nilaiAkhir = (0.3 * nilaiTugas) + (0.3 * nilaiUTS) + (0.4 * nilaiUAS);
        }
        
        // Getter dan setter
        public String getNpm() {
            return npm;
        }
        
        public void setNpm(String npm) {
            this.npm = npm;
        }
        
        public String getNama() {
            return nama;
        }
        
        public void setNama(String nama) {
            this.nama = nama;
        }
        
        public double getNilaiTugas() {
            return nilaiTugas;
        }
        
        public void setNilaiTugas(double nilaiTugas) {
            this.nilaiTugas = nilaiTugas;
            hitungNilaiAkhir();
        }
        
        public double getNilaiUTS() {
            return nilaiUTS;
        }
        
        public void setNilaiUTS(double nilaiUTS) {
            this.nilaiUTS = nilaiUTS;
            hitungNilaiAkhir();
        }
        
        public double getNilaiUAS() {
            return nilaiUAS;
        }
        
        public void setNilaiUAS(double nilaiUAS) {
            this.nilaiUAS = nilaiUAS;
            hitungNilaiAkhir();
        }
        
        public double getNilaiAkhir() {
            return nilaiAkhir;
        }
        
        @Override
        public String toString() {
            return npm + "," + nama + "," + nilaiTugas + "," + nilaiUTS + "," + 
                   nilaiUAS + "," + String.format("%.2f", nilaiAkhir);
        }
        
        public String toFileString() {
            return npm + "," + nama + "," + nilaiTugas + "," + nilaiUTS + "," + nilaiUAS;
        }
        
        @Override
        public int compareTo(Student other) {
            // Untuk pengurutan dari nilai tertinggi ke terendah (descending)
            return Double.compare(other.getNilaiAkhir(), this.nilaiAkhir);
        }
    }
    
    // Class manager untuk mengelola data siswa
    public static class StudentManager {
        private List<Student> students;
        private String filename;
        
        public StudentManager(String filename) {
            this.students = new ArrayList<>();
            this.filename = filename;
            loadStudentsFromFile();
        }
        
        public void loadStudentsFromFile() {
            students.clear();
            File file = new File(filename);
            
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        String[] data = line.split(",");
                        if (data.length >= 5) {
                            String npm = data[0].trim();
                            String nama = data[1].trim();
                            double nilaiTugas = Double.parseDouble(data[2].trim());
                            double nilaiUTS = Double.parseDouble(data[3].trim());
                            double nilaiUAS = Double.parseDouble(data[4].trim());
                            
                            students.add(new Student(npm, nama, nilaiTugas, nilaiUTS, nilaiUAS));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public void saveStudentsToFile() {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                for (Student student : students) {
                    writer.write(student.toFileString());
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public void addStudent(Student student) {
            students.add(student);
            saveStudentsToFile();
        }
        
        public List<Student> getAllStudents() {
            return new ArrayList<>(students);
        }
        
        public void sortStudentsByFinalGrade() {
            Collections.sort(students);
        }
        
        public Student searchStudentByName(String name) {
            // Pertama kita urutkan berdasarkan nama untuk binary search
            List<Student> sortedByName = new ArrayList<>(students);
            sortedByName.sort(Comparator.comparing(Student::getNama));
            
            // Binary search
            int left = 0;
            int right = sortedByName.size() - 1;
            
            while (left <= right) {
                int mid = left + (right - left) / 2;
                Student midStudent = sortedByName.get(mid);
                int comparison = midStudent.getNama().compareToIgnoreCase(name);
                
                if (comparison == 0) {
                    return midStudent; // Nama ditemukan
                } else if (comparison < 0) {
                    left = mid + 1; // Cari di sebelah kanan
                } else {
                    right = mid - 1; // Cari di sebelah kiri
                }
            }
            
            return null; // Tidak ditemukan
        }
        
        // Metode rekursif untuk menghitung total nilai akhir
        public double calculateTotalFinalGradeRecursive(List<Student> students, int index) {
            if (index < 0 || index >= students.size()) {
                return 0;
            }
            return students.get(index).getNilaiAkhir() + calculateTotalFinalGradeRecursive(students, index - 1);
        }
        
        // Menghitung total nilai akhir menggunakan rekursi
        public double calculateTotalFinalGrade() {
            return calculateTotalFinalGradeRecursive(students, students.size() - 1);
        }
        
        // Menghitung rata-rata nilai akhir
        public double calculateAverageFinalGrade() {
            if (students.isEmpty()) {
                return 0;
            }
            return calculateTotalFinalGrade() / students.size();
        }
    }
    
    // Controller untuk FXML
    private StudentManager studentManager;
    private ObservableList<Student> studentData;
    
    @FXML private TableView<Student> tableView;
    @FXML private TableColumn<Student, String> colNpm;
    @FXML private TableColumn<Student, String> colNama;
    @FXML private TableColumn<Student, Double> colTugas;
    @FXML private TableColumn<Student, Double> colUTS;
    @FXML private TableColumn<Student, Double> colUAS;
    @FXML private TableColumn<Student, Double> colAkhir;
    
    @FXML private TextField npmField;
    @FXML private TextField namaField;
    @FXML private TextField nilaiTugasField;
    @FXML private TextField nilaiUTSField;
    @FXML private TextField nilaiUASField;
    @FXML private TextField searchField;
    @FXML private Label totalLabel;
    @FXML private Label averageLabel;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("student.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            
            // Set scene
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("Aplikasi Manajemen Data Siswa");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void initialize() {
        studentManager = new StudentManager("siswa.csv");
        studentData = FXCollections.observableArrayList();
        
        // Setup table columns
        colNpm.setCellValueFactory(new PropertyValueFactory<>("npm"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colTugas.setCellValueFactory(new PropertyValueFactory<>("nilaiTugas"));
        colUTS.setCellValueFactory(new PropertyValueFactory<>("nilaiUTS"));
        colUAS.setCellValueFactory(new PropertyValueFactory<>("nilaiUAS"));
        colAkhir.setCellValueFactory(new PropertyValueFactory<>("nilaiAkhir"));
        
        // Set table data
        tableView.setItems(studentData);
        
        // Load initial data
        refreshTableData();
        updateStats();
    }
    
    @FXML
    private void handleAddStudent() {
        try {
            String npm = npmField.getText().trim();
            String nama = namaField.getText().trim();
            double nilaiTugas = Double.parseDouble(nilaiTugasField.getText().trim());
            double nilaiUTS = Double.parseDouble(nilaiUTSField.getText().trim());
            double nilaiUAS = Double.parseDouble(nilaiUASField.getText().trim());
            
            if (npm.isEmpty() || nama.isEmpty()) {
                showAlert("Error", "NPM dan Nama harus diisi!");
                return;
            }
            
            if (nilaiTugas < 0 || nilaiTugas > 100 || 
                nilaiUTS < 0 || nilaiUTS > 100 || 
                nilaiUAS < 0 || nilaiUAS > 100) {
                showAlert("Error", "Nilai harus di antara 0 dan 100!");
                return;
            }
            
            Student newStudent = new Student(npm, nama, nilaiTugas, nilaiUTS, nilaiUAS);
            studentManager.addStudent(newStudent);
            
            clearInputFields();
            refreshTableData();
            updateStats();
            
            showAlert("Sukses", "Data siswa berhasil ditambahkan!");
        } catch (NumberFormatException e) {
            showAlert("Error", "Format nilai tidak valid! Masukkan angka.");
        }
    }
    
    @FXML
    private void handleSortStudents() {
        studentManager.sortStudentsByFinalGrade();
        refreshTableData();
    }
    
    @FXML
    private void handleSearchStudent() {
        String searchName = searchField.getText().trim();
        
        if (searchName.isEmpty()) {
            showAlert("Error", "Masukkan nama untuk dicari!");
            return;
        }
        
        Student foundStudent = studentManager.searchStudentByName(searchName);
        
        if (foundStudent != null) {
            // Tampilkan hanya siswa yang ditemukan
            studentData.clear();
            studentData.add(foundStudent);
            
            // Highlight siswa yang ditemukan
            tableView.getSelectionModel().select(foundStudent);
            tableView.scrollTo(foundStudent);
            
            showAlert("Sukses", "Siswa dengan nama '" + searchName + "' ditemukan!");
        } else {
            showAlert("Info", "Siswa dengan nama '" + searchName + "' tidak ditemukan.");
            // Kembalikan tampilan semua data
            refreshTableData();
        }
    }
    
    @FXML
    private void handleRefresh() {
        refreshTableData();
    }
    
    private void refreshTableData() {
        studentData.clear();
        studentData.addAll(studentManager.getAllStudents());
        updateStats();
    }
    
    private void updateStats() {
        double total = studentManager.calculateTotalFinalGrade();
        double average = studentManager.calculateAverageFinalGrade();
        
        totalLabel.setText(String.format("Total Nilai Akhir: %.2f", total));
        averageLabel.setText(String.format("Rata-rata Nilai Akhir: %.2f", average));
    }
    
    private void clearInputFields() {
        npmField.clear();
        namaField.clear();
        nilaiTugasField.clear();
        nilaiUTSField.clear();
        nilaiUASField.clear();
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
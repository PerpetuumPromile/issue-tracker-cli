# 📝 Issue Tracker CLI (Spring Boot + Google Sheets)

This is a CLI application that stores and manages issues inside a **Google Sheets document**.  
It is built with **Java (Spring Boot)**, packaged with **Maven**, and runs in **Docker**.

---

## ⚙️ Requirements

Before you can run the app, make sure you have installed:

- **Java 17+** (only required if you run locally without Docker)  
- **Maven 3.9+** (only required if you build/run locally without Docker)  
- **Docker** (recommended way to run, Java + Maven are not required inside the container)

---

## 🔑 Google Setup

1. **Google Cloud Project**
   - Enable **Google Sheets API**
   - Create a **Service Account** with access
   - Download the **`credentials.json`** file

2. **Google Sheet**
   - Create a spreadsheet in Google Drive
   - Share it with your **Service Account email** (from `credentials.json`)
   - Copy the **Sheet ID** from the URL:
     ```
     https://docs.google.com/spreadsheets/d/<SHEET_ID>/edit
     ```

---

## 📂 Project Setup

1. **Clone the repository:**
   ```
   git clone https://github.com/your-username/issue-tracker.git
   cd issue-tracker
   ```

2. **Create a .env file in the project root with your Google Sheet ID:**
   ```
    GOOGLE_SHEETS_SPREADSHEET_ID=<your-google-sheet-id>
   ```
3. **Place the downloaded credentials.json file into:**
   ```
    src/main/resources/credentials.json
   ```


## 🐳 Running with Docker
1. **Build the Docker image**
    ```docker build -t issue-tracker``` .

2. **Run the CLI**

    - Create a new issue
        ```docker run --rm --env-file .env issue-tracker create --description "My first issue"```

    - With optional parent ID:

        ```docker run --rm --env-file .env issue-tracker create --description "Subtask" --parentId ISSUE-1```

    - Update an issue
        ```docker run --rm --env-file .env issue-tracker update --id ISSUE-1 --status CLOSED```

    - List issues
       ``` docker run --rm --env-file .env issue-tracker list --status OPEN```

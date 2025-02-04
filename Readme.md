## Privacy Policy

This extension does not collect or store any personally identifiable information, health data, financial information, authentication credentials, or any other personal data. The only data processed by the extension is local storage for progress tracking on the Codeforces profile page, which is not shared or transferred to any third parties.

By using this extension, you agree that no data will be collected or shared by the developer. For more information or concerns, please contact us at deepsalunkhee@gmail.com.

## Demo 

[![Demo](https://img.youtube.com/vi/Y6zNmKWVDSU/0.jpg)](https://www.youtube.com/watch?v=Y6zNmKWVDSU)


# 🚀 Codeforces Daily Question Extension

## 📌 Overview
The **Codeforces Daily Question** extension is designed to help competitive programmers maintain consistency by providing a structured problem-solving schedule. This browser extension integrates with Codeforces, dynamically recommending daily problems based on the user's rating and selected topic.

## 🎯 Features
- 📅 **Daily Questions Table**: Adds a 7-day problem table to the Codeforces profile page.
- 📚 **Topic Selection**: Users can choose the topic they want to focus on each week.
- 🎯 **Adaptive Difficulty**: Questions are selected based on the user's current Codeforces rating.
- 📈 **Progress Tracking**: Users can input and review their weekly progress.

## 🏗️ Project Structure

### **1️⃣ Browser Extension**
- Injects a UI component into the Codeforces profile page.
- Displays a table with daily problem recommendations.
- Allows topic selection for problem filtering.

### **2️⃣ Client (Frontend)**
#### Note: I was going to build it then though who realy cares of this ,It does not add anything more to the functionality of the extension so I decided to skip it for time being .
- Developed in **Angular 19**.
- Provides a UI for users to track their weekly progress.
- Connects to the backend for fetching problem recommendations.

### **3️⃣ Server (Backend)**
- Built with **Spring Boot**.
- Fetches problems from the **Codeforces API**.
- Implements logic for selecting questions based on user rating and topic preference.
- Stores user progress in **MongoDB**.

## 🔧 Tech Stack
| Component  | Technology |
|------------|-------------|
| **Frontend**  | Angular 19 (Not implemented) |
| **Extension**  | JavaScript, HTML, CSS (Nothing fancy here)|
| **Backend**  | Spring Boot |
| **Database**  | MongoDB |
| **API Source** | Codeforces API |



### 4️⃣ Install Browser Extension
- **Chrome web Store** : [Codeforces Daily Question](https://chromewebstore.google.com/detail/codeforces-daily-question/cnhblbpmgfmplcmmcbhpjpcippjeibak?authuser=0&hl=en)

## 📌 Usage
1. Open your **Codeforces profile page**.
2. Select a **topic** for the week.
3. Solve the recommended daily questions.
4. Update your **progress** via the client interface.

## 🤝 Contributing
Feel free to submit issues and pull requests. Let's improve this extension together! 🚀

## 📜 License
This project is licensed under the **MIT License**.

---
Made with ❤️ for the **competitive programming community** by [deepsalunkhee](https://deepsalunkhee.com)!




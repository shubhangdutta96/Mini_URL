# URL Shortener Service

A simple URL shortener service built using **Scala**, **Akka HTTP**, and **Slick** with **PostgreSQL** as the database.

## 🚀 Features

- Shorten long URLs into short codes
- Optional expiry date for shortened URLs (stores only the **date**, not time)
- Tracks click count for each URL
- Prevents duplicate entries for the same original URL with the same expiry
- Built with **functional programming practices**

## 🛠 Tech Stack

- **Language:** Scala  
- **Backend:** Akka HTTP  
- **Database:** PostgreSQL  
- **ORM:** Slick  
- **JSON:** Spray JSON / Lift JSON
- **Containerization:** Docker (see `docker-compose.yml` and `Dockerfile`)

## Run Locally

### 1. Clone the repo

```bash
git clone https://github.com/shubhangdutta96/Mini_URL.git
```
## 🐳 Configuration to run the service on docker container 
```bash
docker-compose up -d
```

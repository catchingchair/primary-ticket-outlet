# Quickstart

Instructions for running the full stack with only Docker Desktop.

## 1. Prerequisites
- Install Docker Desktop from <https://www.docker.com/products/docker-desktop>.
- Ensure Docker Desktop is running.

## 2. Start
```bash
cd primary-ticket-outlet
docker compose up -d --build
```
Visit <http://localhost:3000>.

## 3. Stop Everything
```bash
docker compose down
```
Use `docker compose down -v` to wipe the database volume.  
In Docker Desktop: Containers tab → select project → Stop (or Delete) buttons.

#!/bin/bash

CONTAINER_NAME="my-mongo"
MONGO_IMAGE="mongo"
MONGO_PORT="27017:27017"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}--- checks is Docker installed... ---${NC}"

# Check 1: Is Docker installed?
if ! command -v docker &> /dev/null
then
    echo -e "${RED}ERROR: Docker command not found.${NC}"
    echo -e "Please install Docker Desktop and ensure it's in your PATH."
    exit 1
fi

# Check 2: Is the Docker daemon running?
# We do this by running a simple docker command (like 'docker info' or 'docker ps')
if ! docker ps &> /dev/null
then
    echo -e "${RED}ERROR: Docker daemon is not running.${NC}"
    echo -e "Please start Docker Desktop and try again."
    exit 1
fi

echo -e "${GREEN}Docker is installed and running.${NC}"
echo ""

# --- 3. Check and Start MongoDB Container ---
echo -e "${BLUE}--- Checking for MongoDB container '$CONTAINER_NAME'... ---${NC}"

# Check if the container is already running
if [ "$(docker ps -q -f "name=^${CONTAINER_NAME}$")" ]; then
    echo -e "${GREEN}MongoDB container '$CONTAINER_NAME' is already running.${NC}"
    
# Check if the container exists but is stopped
elif [ "$(docker ps -aq -f "name=^${CONTAINER_NAME}$")" ]; then
    echo -e "${YELLOW}MongoDB container '$CONTAINER_NAME' exists but is stopped. Starting it...${NC}"
    docker start $CONTAINER_NAME
    
# If the container does not exist, create and run it
else
    echo -e "${YELLOW}MongoDB container '$CONTAINER_NAME' not found. Creating and starting a new one...${NC}"
    docker run -d -p $MONGO_PORT --name $CONTAINER_NAME $MONGO_IMAGE
fi

echo -e "${GREEN}--- Database is ready ---${NC}"
echo ""

# --- 4. Start the Spring Boot Application ---
echo -e "${BLUE}--- Starting Spring Boot application (make sure application.properties uses 'mongodb://localhost:27017') ---${NC}"

# Use the Maven wrapper to run the app
./mvnw spring-boot:run
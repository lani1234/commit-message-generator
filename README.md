# AI-Powered Commit Message Generator

A Spring Boot CLI tool that uses Claude AI to automatically generate professional, meaningful commit messages from your git diffs.

##  What It Does

This tool analyzes your staged git changes and uses Anthropic's Claude API to generate well-formatted, professional commit messages that follow conventional commit standards.

**Example:**
```bash
# You stage some changes
git add MyFile.java

# Run the tool
mvn spring-boot:run

# Get a professional commit message like:
feat: add user authentication validation

- Implemented email format validation
- Added password strength requirements  
- Updated user registration flow
```

## Tech Stack

- **Java 21** - Language
- **Spring Boot 3.2.2** - Application framework
- **Maven** - Build tool
- **Anthropic Claude API** - AI model (Claude Sonnet 4)
- **OkHttp** - HTTP client
- **Gson** - JSON processing
- **Lombok** - Boilerplate reduction

## Prerequisites

- Java 21 or later
- Maven 3.6+
- Git
- Anthropic API key ([Get one free](https://console.anthropic.com))

## Setup

### 1. Clone the Repository
```bash
http:
git clone https://github.com/lani1234/commit-message-generator.git
or ssh:
git clone git@github.com:lani1234/commit-message-generator.git

cd commit-message-generator
```

### 2. Get Your API Key

1. Go to [console.anthropic.com](https://console.anthropic.com)
2. Sign up (you get $5 in free credits)
3. Generate an API key
4. Copy the key

### 3. Set Your API Key
```bash
export ANTHROPIC_API_KEY="your-api-key-here"
```

**Optional:** Add to your `~/.zprofile` to make it permanent:
```bash
echo 'export ANTHROPIC_API_KEY="your-api-key-here"' >> ~/.zprofile
source ~/.zprofile
```

### 4. Build the Project
```bash
mvn clean install
```

## How To Run

### Option 1: Basic Usage
```bash
# 1. Make some changes to your code
echo "// New feature" >> MyFile.java

# 2. Stage the changes
git add MyFile.java

# 3. Generate a commit message
mvn spring-boot:run
```

The tool will output a formatted commit message that you can copy and use.

### Option 2: Using the JAR Directly
```bash
# Build once
mvn clean package

# Then run the JAR
java -jar target/commit-message-generator-1.0.0.jar
```

### Option 3: Create a Convenient Script

Create `generate-commit.sh`:
```bash
#!/bin/bash
export ANTHROPIC_API_KEY="your-key-here"
cd "$(dirname "$0")"
mvn spring-boot:run
```

Make it executable:
```bash
chmod +x generate-commit.sh
./generate-commit.sh
```

## Cost

Each commit message generation costs approximately **$0.001 - $0.003** (less than a penny).

With the free $5 credits, you can generate 1,500+ commit messages.


##  Troubleshooting

### "No staged changes found"
Make sure you've staged files with `git add` before running the tool.

### "ANTHROPIC_API_KEY not set"
Set your API key as an environment variable:
```bash
export ANTHROPIC_API_KEY="your-key"
```

### Java Version Issues
This project requires Java 21. Check your version:
```bash
java -version
```

Set Java 21 as default:
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
```

### Build Errors with Lombok
Make sure annotation processing is enabled in your IDE:
- **IntelliJ**: Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable

## Future Enhancements

Ideas for extending this project:

- [ ] Interactive mode (accept/reject/regenerate messages)
- [ ] Auto-commit option (`--commit` flag)
- [ ] Multiple commit message styles (conventional, emoji, detailed)
- [ ] Git hooks integration (automatic generation on commit)
- [ ] Configuration file for custom prompt templates
- [ ] Batch mode for multiple commits
- [ ] Web UI with Spring MVC
- [ ] REST API for IDE plugins


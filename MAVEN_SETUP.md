# Maven Setup for GitHub Packages

## Settings.xml Configuration

Make sure your `~/.m2/settings.xml` (or `%USERPROFILE%\.m2\settings.xml` on Windows) has the following configuration:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```

## Important Notes:

1. **Server ID must match**: The `<id>github</id>` in settings.xml must exactly match the `<id>github</id>` in the POM files.

2. **Token Permissions**: Your GitHub Personal Access Token needs:
   - `read:packages` permission (minimum)
   - `write:packages` permission (if you want to deploy)

3. **Token Format**: Use your actual GitHub Personal Access Token, not your password.

4. **Verify Settings Location**: 
   - Windows: `C:\Users\<YourUsername>\.m2\settings.xml`
   - Linux/Mac: `~/.m2/settings.xml`

## Troubleshooting

If you're still getting 401 errors:

1. **Verify settings.xml is being read**:
   ```bash
   mvn help:effective-settings
   ```
   This will show you if Maven is reading your settings.xml

2. **Check server ID matches**:
   - POM files use: `<id>github</id>`
   - settings.xml must use: `<id>github</id>`

3. **Test authentication manually**:
   ```bash
   curl -u YOUR_USERNAME:YOUR_TOKEN https://maven.pkg.github.com/SystAttic/TraversiumCommonMultitenancy/traversium/common-multitenancy/1.1.0/
   ```

4. **Clear Maven cache** (if needed):
   ```bash
   mvn dependency:purge-local-repository
   ```

5. **Use environment variables** (alternative):
   ```bash
   export GITHUB_ACTOR=your-username
   export GITHUB_TOKEN=your-token
   mvn clean install
   ```

## Creating a GitHub Personal Access Token

1. Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token (classic)
3. Select scopes: `read:packages` and/or `write:packages`
4. Copy the token and use it in your settings.xml


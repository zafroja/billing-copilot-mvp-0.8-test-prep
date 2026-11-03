# Start here for the current deliverable

**1. See working billing demo:** Open `billing-copilot-demo-preview.html`. It uses the previously verified Jobber export, not live Jobber.\
**2. Verify locally:** `python3 deploy/verify_offline.py` (requires PyYAML + Java 17 JDK).\
**3. Full build checks:** Upload this folder to a private GitHub repository and open **Actions** → **Verify Billing Copilot private-pilot build**. That pipeline downloads Java and React dependencies, executes JUnit, runs PostgreSQL migrations, and compiles React.\
**4. Secure staging only after green CI:** Follow `DEPLOYMENT_AND_BUILD_REPORT.md` and `docker-compose.private-pilot.yml`.\
**5. Only then:** authorize the existing Jobber test account via the deployed app. Never paste client secrets or access tokens into chat.

Do not claim this package is a production-ready SaaS. The full builds and live connection have not run in this offline environment.

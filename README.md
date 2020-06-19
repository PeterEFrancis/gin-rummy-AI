# gin-rummy-AI

### To Use:

- Clone the repository locally
- Import 'Existing Maven Project' "GinRummy-maven" into Eclipse
- Edit the build path to include "src/main/java/h20-genmodel.jar"
- You may need to install maven and/or dl4j
- To use the data processing jupyter notebooks in the "data" folder, you must first have jupyter installed. If you are generating linear regression coeficients, these coefficients must be on the line of "regression_models/linear_coef.csv" that corresponds to the player `version` (0-based).

- (Optional) In order to use the `WEB` Player type you must first start the python server in the "web" folder, and change `post_url` in BlackBox to point to your server.

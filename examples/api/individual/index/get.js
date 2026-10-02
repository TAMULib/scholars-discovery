/**
 * URLs used to retrieve profile data and support the display of Scholars profiles.
 */
const limit = 15;

const fl = [
  'id',
  'firstName',
  'lastName',
  'positions',
  'positionOrganization',
  'image',
  'thumbnail',
  'preferredTitle',
  'modTime',
  'primaryEmail'
].join(",");

const fq = 'class:Person';

const apiBaseUrl = 'http://localhost:9000/';

const apiPath = 'individual/search/recentlyUpdated/';

const apiUrl = `${apiBaseUrl}${apiPath.replace(/\/$/, "")}?limit=${limit}&fl=${fl},class&fq=${fq}`;

const imageBaseUrl = 'https://api.library.tamu.edu/scholars-discovery/';

const profileBaseUrl = 'https://scholars.library.tamu.edu/vivo/display/';

const altImage = 'https://energy.tamu.edu/wp-content/plugins/wp-scholars-tamu/images/TAM-LogoBox.jpg';

/**
 * Calls getScholarsProfile() when the Load Scholars button is clicked.
 */
const button = document.getElementById("load-scholars");

button.addEventListener("click", getScholarsProfile);

/**
 * Function to fetch recently updated Scholars profiles and renders them in the Scholars container.
 */
async function getScholarsProfile() {

  const container = document.getElementById('scholars-container');

  try {
    const request = await fetch(apiUrl);

    if (!request.ok) {
      throw new Error(`HTTP Error! status: ${request?.status}`);
    }

    const response = await request.json();

    const scholarsProfile = response?._embedded?.individual;

    const htmlContent = scholarsProfile?.map(createScholarsProfile)?.join('');

    container.innerHTML = htmlContent;

  } catch(error) {
    console.error('Failed to fetch data:', error);
  }
}

/**
 * Function to create the HTML for a Scholars profile.
 * 
 * @param scholar Individual JSON object.
 */
function createScholarsProfile(scholar) {

  const profileImage = scholar?.image || scholar?.thumbnail;

  const imageUrl = profileImage
    ? `${imageBaseUrl}${profileImage.replace(/^\/+/, "")}`
    : null;

  const displayImage = imageUrl
    ? imageUrl
    : altImage;

  const profileUrl = `${profileBaseUrl}${scholar?.id}`;

  const modTimeConversion = new Date(scholar?.modTime) || "N/A";

  /**
   * Retrieves positions and uses the first organization associated with each position from the Solr data.
   */
  const positions = scholar?.positions ?? [];

  const formattedPositions = positions.map(position => {

    const organization = position?.organizations?.[0];

    const organizationLabel = organization?.label ?? "N/A";

    return `${organizationLabel}`;
  });

  const positionOrganization = formattedPositions?.join(", ") || "N/A";

  return `
    <div onclick="window.open('${profileUrl}', '_blank')">
      <img src="${displayImage}">

      <h3>
      ${scholar?.firstName ?? "N/A"} ${scholar?.lastName ?? "N/A"}
      </h3>

      <p>
      <strong>Preferred Title:</strong> ${scholar?.preferredTitle ?? "N/A"}
      </p>

      <p>
      <strong>Positions:</strong> ${positionOrganization}
      </p>

      <p>
      <strong>Primary Email:</strong> ${scholar?.primaryEmail ?? "N/A"}
      </p>

      <p>
      <strong>Last Updated:</strong> ${modTimeConversion}
      </p>
    </div>
  `;

}

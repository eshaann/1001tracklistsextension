document.addEventListener('DOMContentLoaded', function() {
    const generateButton = document.getElementById('generateTracklist');
  
    generateButton.addEventListener('click', async () => {
      // Get the active tab's URL
      const [activeTab] = await chrome.tabs.query({
        active: true,
        currentWindow: true
      });
  
      // Check if the URL is a YouTube video page
      if (activeTab.url.includes("youtube.com/watch")) {
        // Extract the video ID from the URL
        const url = new URL(activeTab.url);
        const videoId = url.searchParams.get("v");
        
        if (videoId) {
          // Here, you would send the videoId to your backend API
          // This is a placeholder for your API call
          console.log(`Sending video ID: ${videoId} to backend for processing.`);
          
          const apiUrl = 'YOUR_BACKEND_API_ENDPOINT'; // Replace with your actual API endpoint
          
          try {
            const response = await fetch(apiUrl, {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
              },
              body: JSON.stringify({ videoId: videoId }),
            });
  
            if (!response.ok) {
              throw new Error('API request failed');
            }
  
            const tracklistData = await response.json();
            console.log('Received tracklist from API:', tracklistData);
  
            // Now, you'll need to inject a script to draft the comment on the YouTube page.
            // This is a crucial step as the popup script can't access the YouTube page's DOM directly.
            chrome.scripting.executeScript({
              target: { tabId: activeTab.id },
              function: draftComment,
              args: [tracklistData]
            });
  
          } catch (error) {
            console.error('Error generating tracklist:', error);
            alert('Failed to generate tracklist. Please try again.');
          }
        } else {
          alert("This is not a valid YouTube video page.");
        }
      } else {
        alert("Please navigate to a YouTube video page to use this extension.");
      }
    });
  });
  
  // This function is executed in the context of the YouTube page itself.
  function draftComment(tracklistData) {
    // Find the comment box element on the page
    const commentBox = document.querySelector("#comments #contenteditable-root");
  
    if (commentBox) {
      // Format the tracklist for the comment
      let commentText = "Generated Tracklist:\n\n";
      tracklistData.tracks.forEach(track => {
        commentText += `${track.timestamp} - ${track.artist} - ${track.title}\n`;
      });
  
      // Set the comment box value and display a message
      commentBox.textContent = commentText;
      alert("Tracklist drafted! You can now edit and post the comment.");
    } else {
      alert("Could not find the comment box.");
    }
  }
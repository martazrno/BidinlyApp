window.multiUpload = {
    uploadFiles: async function (inputId, uploadUrl) {

        const input = document.getElementById(inputId);
        if (!input || !input.files || input.files.length === 0) {
            return [];
        }

        const formData = new FormData();
        for (const file of input.files) {
            console.log("JS File:", file.name, "size:", file.size);
            formData.append("files", file, file.name);
        }

        const response = await fetch(uploadUrl, {
            method: "POST",
            body: formData
        });

        if (!response.ok) {
            let errorText = await response.text();
            console.error("Upload error response:", errorText);
            throw new Error("Upload failed: " + errorText);
        }
        
        const text = await response.text();
        console.log("Server response text:", text);

        try {
            return JSON.parse(text);
        } catch (err) {
            console.error("JSON parse failed:", err);
            throw new Error("Upload succeeded but response JSON is invalid.");
        }
    }
};
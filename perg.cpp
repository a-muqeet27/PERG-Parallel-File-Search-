#include <iostream>
#include <fstream>
#include <string>
#include <queue>
#include <unistd.h>
#include <regex>
#include <dirent.h>
#include <cstdlib>
#include <string.h>
#include <sstream>
#include <algorithm>
#include <omp.h>

struct Settings {
	Settings(): recursive(), invert(), verbose(), isFile(), fileWise(), checkHidden(), extra(), caseSensitive(), numExtra(), file(), term() {}
	bool recursive;
	bool invert;
	bool verbose;
	bool isFile;
	bool fileWise;
	bool checkHidden;
	bool extra;
	bool caseSensitive;
	int numExtra;
	std::string file;
	std::string term;
};

void helpCheck(char *argv[]) {
	if (argv[1] == std::string("-h") || argv[1] == std::string("--help") || argv[1] == std::string("-help")) {
		std::cout << "    Modes:\n";
		std::cout << "    -A    After Context         Number of lines after match to display\n";
		std::cout << "    -f    Single File Search    Search only one file\n";
		std::cout << "    -i    Include Hidden        Include hidden files\n";
		std::cout << "    -r    Recursive Search      Search subdirectories\n";
		std::cout << "    -v    Invert Match          Print lines that do NOT match\n";
		std::cout << "    -V    Verbose Output        Print file path before match\n";
		std::cout << "    -w    File Parallelism      Use parallelism across files\n";
		std::cout << "    -c    Case Sensitive        Make search case-sensitive\n";
		exit(0);
	}
}

void getSettings(int argc, char *argv[], Settings *instance) {
	std::queue<std::string> settings;
	for (int i = 1; i < argc; i++) settings.push(argv[i]);

	while (!settings.empty()) {
		std::string arg = settings.front();
		if (arg == "-r") instance->recursive = true;
		else if (arg == "-v") instance->invert = true;
		else if (arg == "-V") instance->verbose = true;
		else if (arg == "-c") instance->caseSensitive = true;
		else if (arg == "-f") {
			settings.pop();
			if (!settings.empty()) {
				std::string nextArg = settings.front();
				if (nextArg[0] == '-') {
					std::cerr << "ERROR: Missing file path after -f option.\n";
					exit(1);
				}
				instance->isFile = true;
				instance->file = nextArg;
			} else {
				std::cerr << "ERROR: Missing file path after -f option.\n";
				exit(1);
			}
		} else if (arg == "-w") instance->fileWise = true;
		else if (arg == "-i") instance->checkHidden = true;
		else if (arg == "-A") {
			settings.pop();
			if (!settings.empty()) {
				std::string nextArg = settings.front();
				try {
					instance->numExtra = std::stoi(nextArg);
					if (instance->numExtra < 0) {
						std::cerr << "ERROR: After context number must be non-negative.\n";
						exit(1);
					}
					instance->extra = true;
				} catch (const std::exception& e) {
					std::cerr << "ERROR: Invalid number after -A option: " << nextArg << "\n";
					exit(1);
				}
			} else {
				std::cerr << "ERROR: Missing number after -A option.\n";
				exit(1);
			}
		} else {
			if (settings.size() > 1) {
				std::cerr << "ERROR: Invalid usage. Multiple search terms provided.\n";
				std::cerr << "Use 'perg -h' for help.\n";
				exit(1);
			}
			instance->term = arg;
		}
		settings.pop();
	}

	if (instance->term.empty()) {
		std::cerr << "ERROR: Search term not provided.\n";
		std::cerr << "Use 'perg -h' for help.\n";
		exit(1);
	}
}

char cwd [PATH_MAX];

// Function to check if a path is absolute (works for both Unix and Windows)
bool isAbsolutePath(const std::string& path) {
	if (path.empty()) return false;
	// Unix absolute path
	if (path[0] == '/') return true;
	// Windows absolute path (e.g., C:\, D:\)
	if (path.length() >= 2 && path[1] == ':') return true;
	return false;
}

std::regex createRegex(const std::string &term, bool caseSensitive) {
	try {
		if (caseSensitive)
			return std::regex(term);
		else
			return std::regex(term, std::regex_constants::icase);
	} catch (const std::regex_error& e) {
		std::cerr << "ERROR: Invalid regular expression: " << term << "\n";
		exit(1);
	}
}

void printSingle(std::queue<std::string> *filePaths, Settings *instance, bool &found) {
	while (!filePaths->empty()) {
		std::string currentFile = filePaths->front();
		filePaths->pop();
		std::ifstream file(currentFile);
		
		if (!file.is_open()) {
			std::cerr << "WARNING: Cannot open file: " << currentFile << "\n";
			continue;
		}
		
		std::vector<std::string> lines;
		std::string line;
		while (std::getline(file, line)) lines.push_back(line);
		file.close();

		if (lines.empty()) continue;

		std::regex rgx = createRegex(instance->term, instance->caseSensitive);
		int numThreads = omp_get_max_threads();
		unsigned long long total = lines.size();
		unsigned long long blockSize = total / numThreads + 1;

		#pragma omp parallel for schedule(static)
		for (int i = 0; i < numThreads; ++i) {
			unsigned long long start = i * blockSize;
			unsigned long long end = std::min(start + blockSize, total);
			for (unsigned long long j = start; j < end; ++j) {
				std::string output;
				std::string &curr = lines[j];
				bool match = std::regex_search(curr, rgx);

				if ((!match && instance->invert) || (match && !instance->invert)) {
					found = true;
					if (instance->verbose)
						output += currentFile + ": ";
					output += curr + "\n";

					if (instance->extra && match && !instance->invert) {
						for (int k = 1; k <= instance->numExtra && (j + k) < total; ++k) {
							output += lines[j + k] + "\n";
							if (std::regex_search(lines[j + k], rgx)) k = 0;
						}
						output += "--\n";
					}
					#pragma omp critical
					std::cout << output;
				}
			}
		}
	}
}

void printMultiple(std::queue<std::string> *filePaths, Settings *instance, bool &found) {
	std::regex rgx = createRegex(instance->term, instance->caseSensitive);
	int totalFiles = filePaths->size();
	
	#pragma omp parallel for schedule(dynamic)
	for (int i = 0; i < totalFiles; ++i) {
		std::string fileName;
		#pragma omp critical
		{
			if (!filePaths->empty()) {
				fileName = filePaths->front();
				filePaths->pop();
			}
		}
		
		if (fileName.empty()) continue;
		
		std::ifstream file(fileName);
		if (!file.is_open()) {
			#pragma omp critical
			std::cerr << "WARNING: Cannot open file: " << fileName << "\n";
			continue;
		}
		
		std::string line, output;
		std::vector<std::string> fileLines;
		while (std::getline(file, line)) fileLines.push_back(line);
		file.close();
		
		for (size_t lineNum = 0; lineNum < fileLines.size(); ++lineNum) {
			const std::string& currentLine = fileLines[lineNum];
			bool match = std::regex_search(currentLine, rgx);
			
			if ((!match && instance->invert) || (match && !instance->invert)) {
				found = true;
				std::string lineOutput;
				if (instance->verbose)
					lineOutput += fileName + ": ";
				lineOutput += currentLine + "\n";
				
				if (instance->extra && match && !instance->invert) {
					for (int j = 1; j <= instance->numExtra && (lineNum + j) < fileLines.size(); ++j) {
						lineOutput += fileLines[lineNum + j] + "\n";
						if (std::regex_search(fileLines[lineNum + j], rgx)) j = 0;
					}
					lineOutput += "--\n";
				}
				
				#pragma omp critical
				std::cout << lineOutput;
			}
		}
	}
}

void findAll(std::queue<std::string> *filePaths, const char *cwd, Settings *instance) {
	DIR *dir = opendir(cwd);
	struct dirent *ent;
	if (!dir) {
		std::cerr << "WARNING: Cannot open directory: " << cwd << "\n";
		return;
	}
	
	while ((ent = readdir(dir)) != NULL) {
		std::string name = ent->d_name;
		if (name == "." || name == "..") continue;
		if (!instance->checkHidden && name[0] == '.') continue;
		
		std::string path = std::string(cwd) + "/" + name;
		DIR *sub = opendir(path.c_str());
		if (sub) {
			closedir(sub);
			if (instance->recursive) findAll(filePaths, path.c_str(), instance);
		} else {
			filePaths->push(path);
		}
	}
	closedir(dir);
}

int main(int argc, char *argv[]) {
	if (argc < 2) {
		std::cerr << "ERROR: No arguments provided.\n";
		std::cerr << "Use 'perg -h' for help.\n";
		return 1;
	}
	
	Settings *instance = new Settings;
	std::queue<std::string> *filePaths = new std::queue<std::string>;
	bool found = false;

	helpCheck(argv);
	getSettings(argc, argv, instance);
	
	if (getcwd(cwd, PATH_MAX) == NULL) {
		std::cerr << "ERROR: Cannot get current working directory.\n";
		delete filePaths;
		delete instance;
		return 1;
	}

	if (instance->isFile) {
		// Handle absolute and relative paths properly for both Unix and Windows
		std::string filePath = instance->file;
		if (!isAbsolutePath(filePath)) {
			filePath = std::string(cwd) + "/" + instance->file;
		}
		filePaths->push(filePath);
		printSingle(filePaths, instance, found);
	} else {
		findAll(filePaths, cwd, instance);
		if (filePaths->empty()) {
			std::cout << "No files found to search.\n";
		} else {
			if (instance->fileWise) printMultiple(filePaths, instance, found);
			else printSingle(filePaths, instance, found);
		}
	}

	if (!found) {
		std::cout << "Search term \"" << instance->term << "\" not found.\n";
	}

	delete filePaths;
	delete instance;
	return 0;
}